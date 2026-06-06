package com.joegec.joycon2android.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
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
 */
class JoyconConnection(
    private val context: Context,
    val side: Side,
    val deviceName: String,
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
        // Bitmask: 0x01=P1, 0x02=P2, 0x04=P3, 0x08=P4, 0x00=default animation
        private fun playerLedCmd(player: Int): ByteArray {
            val bitmask = if (player > 0) (1 shl (player - 1)).toByte() else 0x00
            return byteArrayOf(
                0x09, 0x91.toByte(), 0x01, 0x07, 0x00, 0x08, 0x00, 0x00,
                bitmask, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            )
        }

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
    private var initComplete = false

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    @SuppressLint("MissingPermission")
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
        @SuppressLint("MissingPermission")
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
                    _connectionState.value = JoyconConnectionState(
                        deviceName = deviceName,
                        error = if (status != BluetoothGatt.GATT_SUCCESS) {
                            "Connection lost (status $status)"
                        } else null
                    )
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG, "[$side] MTU=$mtu. Discovering services.")
            g.discoverServices()
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
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
                opQueue.enqueue {
                    val cccd = cmdResponseChar!!.getDescriptor(CCCD)
                    @Suppress("DEPRECATION")
                    cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    g.writeDescriptor(cccd)
                }
            }

            // Subscribe to input notifications
            g.setCharacteristicNotification(notifyChar, true)
            opQueue.enqueue {
                val cccd = notifyChar!!.getDescriptor(CCCD)
                @Suppress("DEPRECATION")
                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                g.writeDescriptor(cccd)
            }
            enqueueInitWrite(g, INIT_CMD_1)
            enqueueInitWrite(g, INIT_CMD_2)

            // After init writes complete, send any pending LED command
            opQueue.enqueue {
                initComplete = true
                val pending = pendingPlayerLed
                pendingPlayerLed = null
                if (pending != null) {
                    @Suppress("DEPRECATION")
                    writeChar!!.value = playerLedCmd(pending.index)
                    @Suppress("DEPRECATION")
                    g.writeCharacteristic(writeChar)
                } else {
                    opQueue.complete()
                }
            }
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            Log.i(TAG, "[$side] CCCD write status=$status")
            opQueue.complete()
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int
        ) {
            mainHandler.postDelayed({ opQueue.complete() }, INIT_GAP_MS)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic
        ) {
            val data = ch.value ?: return
            when (ch.uuid) {
                NOTIFY_CHAR -> PacketParser.parse(data, side)?.let { _input.value = it }
                CMD_RESPONSE_CHAR -> Log.d(TAG, "[$side] Cmd response: ${data.size} bytes")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun setPlayerLed(player: PlayerNumber) {
        if (!initComplete) {
            pendingPlayerLed = player
            return
        }
        val g = gatt ?: return
        enqueueInitWrite(g, playerLedCmd(player.index))
    }

    @SuppressLint("MissingPermission")
    fun clearPlayerLed() {
        pendingPlayerLed = null
        if (!initComplete) return
        val g = gatt ?: return
        enqueueInitWrite(g, playerLedCmd(0))
    }

    @SuppressLint("MissingPermission")
    private fun enqueueInitWrite(g: BluetoothGatt, bytes: ByteArray) {
        opQueue.enqueue {
            @Suppress("DEPRECATION")
            writeChar!!.value = bytes
            @Suppress("DEPRECATION")
            g.writeCharacteristic(writeChar)
        }
    }
}
