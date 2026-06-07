package com.joegec.joycon2android.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.joegec.joycon2android.model.JoyconConnectionState
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Manages a single BLE GATT connection to one Joy-Con 2.
 * Each Joy-Con gets its own instance with independent state.
 *
 * All BLE operations require BLUETOOTH_CONNECT permission, which is verified
 * by the permission launcher in MainActivity before any BLE code is reached.
 */
@SuppressLint("MissingPermission")
class JoyconConnection(
    private val context: Context,
    val side: Side,
    val deviceName: String,
    private val onDisconnected: (() -> Unit)? = null,
) {
    companion object {
        private const val TAG = "Joycon2"

        private val INPUT_SERVICE = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd0")
        private val NOTIFY_CHAR = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd2")
        private val WRITE_CHAR = UUID.fromString("649d4ac9-8eb7-4e6c-af44-1ea54fe5f005")
        private val CMD_RESPONSE_CHAR = UUID.fromString("c765a961-d9d8-4d36-a20a-5315b111836a")
        private val CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val INIT_CMD_1 = byteArrayOf(
            0x0C, 0x91.toByte(), 0x01, 0x02, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        )
        private val INIT_CMD_2 = byteArrayOf(
            0x0C, 0x91.toByte(), 0x01, 0x04, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        )

        // Subcommand 0x07: set LED pattern via bitmask (16 bytes)
        // Lower nibble = solid LEDs (0x01=P1, 0x02=P2, 0x04=P3, 0x08=P4)
        // Upper nibble = flashing LEDs (0x10=P1, 0x20=P2, 0x40=P3, 0x80=P4)
        // 0xF0 = all flashing = default cycling animation
        private fun playerLedCmd(player: Int): ByteArray {
            val bitmask = (1 shl (player - 1)).toByte()
            return byteArrayOf(
                0x09, 0x91.toByte(), 0x01, 0x07, 0x00, 0x08, 0x00, 0x00,
                bitmask, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            )
        }

        // All 4 player LEDs solid on (0x0F = P1+P2+P3+P4)
        private val LED_ALL_ON_CMD = byteArrayOf(
            0x09, 0x91.toByte(), 0x01, 0x07, 0x00, 0x08, 0x00, 0x00,
            0x0F, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        )

        private const val DESIRED_MTU = 247
        private const val INIT_GAP_MS = 500L
    }

    private val _connectionState = MutableStateFlow(
        JoyconConnectionState(connecting = true, deviceName = deviceName)
    )
    val connectionState: StateFlow<JoyconConnectionState> = _connectionState.asStateFlow()

    private val _input = MutableStateFlow(JoyconInput())
    val input: StateFlow<JoyconInput> = _input.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val opQueue = GattOpQueue()
    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private var notifyChar: BluetoothGattCharacteristic? = null
    private var cmdResponseChar: BluetoothGattCharacteristic? = null
    private var pendingPlayerLed: PlayerNumber? = null
    @Volatile var initComplete = false
        private set
    private var ledSentAfterFirstPacket = false

    fun connect(device: BluetoothDevice) {
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        mainHandler.removeCallbacksAndMessages(null)
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        opQueue.clear()
        _connectionState.value = JoyconConnectionState()
        _input.value = JoyconInput()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "[$side] Connected. Requesting MTU $DESIRED_MTU")
                    _connectionState.value = JoyconConnectionState(
                        connected = true, deviceName = deviceName
                    )
                    g.requestMtu(DESIRED_MTU)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.w(TAG, "[$side] Disconnected (status=$status)")
                    opQueue.clear()
                    g.close()
                    gatt = null
                    initComplete = false
                    ledSentAfterFirstPacket = false
                    _connectionState.value = JoyconConnectionState(
                        deviceName = deviceName,
                        error = if (status != BluetoothGatt.GATT_SUCCESS) {
                            "Connection lost (status $status)"
                        } else null
                    )
                    _input.value = JoyconInput()
                    onDisconnected?.invoke()
                }
            }
        }

        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG, "[$side] MTU=$mtu. Discovering services.")
            g.discoverServices()
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            Log.i(TAG, "[$side] Services discovered (status=$status)")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = JoyconConnectionState(
                    error = "Service discovery failed", deviceName = deviceName
                )
                return
            }

            val svc = g.getService(INPUT_SERVICE)
            if (svc == null) {
                _connectionState.value = JoyconConnectionState(
                    error = "Not a compatible Joy-Con 2", deviceName = deviceName
                )
                return
            }

            writeChar = svc.getCharacteristic(WRITE_CHAR)
            notifyChar = svc.getCharacteristic(NOTIFY_CHAR)
            cmdResponseChar = svc.getCharacteristic(CMD_RESPONSE_CHAR)
            if (writeChar == null || notifyChar == null) {
                _connectionState.value = JoyconConnectionState(
                    error = "Missing BLE characteristics", deviceName = deviceName
                )
                return
            }
            writeChar!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

            // Subscribe to command response notifications (required for LED commands)
            if (cmdResponseChar != null) {
                g.setCharacteristicNotification(cmdResponseChar, true)
                val cmdCccd = cmdResponseChar!!.getDescriptor(CCCD)
                if (cmdCccd != null) {
                    opQueue.enqueue {
                        Log.d(TAG, "[$side] Writing CMD_RESPONSE CCCD")
                        writeDescriptor(g, cmdCccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    }
                } else {
                    Log.w(TAG, "[$side] CMD_RESPONSE char has no CCCD descriptor")
                }
            }

            // Subscribe to input notifications
            g.setCharacteristicNotification(notifyChar, true)
            val notifyCccd = notifyChar!!.getDescriptor(CCCD)
            if (notifyCccd != null) {
                opQueue.enqueue {
                    Log.d(TAG, "[$side] Writing NOTIFY CCCD")
                    writeDescriptor(g, notifyCccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                }
            } else {
                Log.e(TAG, "[$side] NOTIFY char has no CCCD descriptor — notifications won't work!")
            }
            enqueueInitWrite(g, INIT_CMD_1)
            enqueueInitWrite(g, INIT_CMD_2)

            opQueue.enqueue {
                initComplete = true
                _connectionState.value = JoyconConnectionState(
                    connected = true, ready = true, deviceName = deviceName
                )
                Log.i(TAG, "[$side] Init sequence complete")
                false // no GATT op — advance immediately
            }
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            Log.i(TAG, "[$side] CCCD write status=$status")
            mainHandler.post { opQueue.complete() }
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int
        ) {
            Log.d(TAG, "[$side] Char write status=$status initComplete=$initComplete")
            val delay = if (initComplete) 0L else INIT_GAP_MS
            mainHandler.postDelayed({ opQueue.complete() }, delay)
        }

        @Deprecated("Deprecated in Java - used for API < 33")
        override fun onCharacteristicChanged(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic
        ) {
            handleCharacteristicChanged(g, ch.uuid, ch.value ?: return)
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, value: ByteArray
        ) {
            handleCharacteristicChanged(g, ch.uuid, value)
        }
    }

    fun setPlayerLed(player: PlayerNumber) {
        pendingPlayerLed = player
        if (!initComplete) return
        val g = gatt ?: return
        opQueue.enqueue { sendLedCommand(g) }
    }

    fun clearPlayerLed() {
        pendingPlayerLed = null
        if (!initComplete) return
        val g = gatt ?: return
        opQueue.enqueue { sendLedCommand(g) }
    }

    private fun sendLedCommand(g: BluetoothGatt): Boolean {
        val pending = pendingPlayerLed
        pendingPlayerLed = null
        val cmd = if (pending != null) playerLedCmd(pending.index) else LED_ALL_ON_CMD
        Log.i(TAG, "[$side] Sending LED cmd: ${cmd.joinToString(" ") { "%02X".format(it) }}")
        return writeCharacteristic(g, writeChar!!, cmd)
    }

    private fun enqueueInitWrite(g: BluetoothGatt, bytes: ByteArray) {
        opQueue.enqueue { writeCharacteristic(g, writeChar!!, bytes) }
    }

    private fun handleCharacteristicChanged(g: BluetoothGatt, uuid: UUID, data: ByteArray) {
        when (uuid) {
            NOTIFY_CHAR -> {
                PacketParser.parse(data, side)?.let { _input.value = it }
                if (!ledSentAfterFirstPacket && initComplete) {
                    ledSentAfterFirstPacket = true
                    mainHandler.post { opQueue.enqueue { sendLedCommand(g) } }
                }
            }
            CMD_RESPONSE_CHAR -> Log.d(TAG, "[$side] Cmd response: ${data.size} bytes")
        }
    }

    private fun writeCharacteristic(
        g: BluetoothGatt,
        ch: BluetoothGattCharacteristic,
        value: ByteArray,
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) ==
                BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            ch.value = value
            @Suppress("DEPRECATION")
            g.writeCharacteristic(ch)
        }
    }

    private fun writeDescriptor(
        g: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        value: ByteArray,
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeDescriptor(descriptor, value) == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = value
            @Suppress("DEPRECATION")
            g.writeDescriptor(descriptor)
        }
    }

}
