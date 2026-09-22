package com.joegec.joycon2android.connection

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
 * Manages a single BLE GATT connection to one Joy-Con 2 or compatible controller.
 * Each Joy-Con gets its own instance with independent state.
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

        private val KEYLINKER_SERVICE = UUID.fromString("d7f010e0-660d-46e9-96c3-19c4148bdab5")
        private val KEYLINKER_WRITE = UUID.fromString("d7f010e1-660d-46e9-96c3-19c4148bdab5")
        private val KEYLINKER_NOTIFY = UUID.fromString("d7f010e2-660d-46e9-96c3-19c4148bdab5")
        private val NYXI_INPUT_NOTIFY_CHAR = UUID.fromString("d5a9e01e-2ffc-4cca-b20c-8b67142bf442")

        private val INIT_CMD_1 = byteArrayOf(
            0x0C, 0x91.toByte(), 0x01, 0x02, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        )
        private val INIT_CMD_2 = byteArrayOf(
            0x0C, 0x91.toByte(), 0x01, 0x04, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        )

        private val SPI_READ_COLOR_CMD = byteArrayOf(
            0x02, 0x91.toByte(), 0x00, 0x04, 0x00, 0x08, 0x00, 0x00,
            0x40, 0x7E, 0x00, 0x00, 0x00, 0x30, 0x01, 0x00
        )

        private fun playerLedCmd(bitmask: Byte): ByteArray {
            return byteArrayOf(
                0x09, 0x91.toByte(), 0x01, 0x07, 0x00, 0x08, 0x00, 0x00,
                bitmask, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            )
        }

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
    private val stickCalibrator = StickCalibrator()
    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private var notifyChar: BluetoothGattCharacteristic? = null
    private var cmdResponseChar: BluetoothGattCharacteristic? = null
    private var pendingPlayerLed: PlayerNumber? = null
    @Volatile var initComplete = false
        private set
    @Volatile private var highPriority = true
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
                        connected = true, deviceName = deviceName, bondState = g.device.bondState
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

            // Subscribe to all notification characteristics across all discovered services
            for (s in g.services) {
                for (char in s.characteristics) {
                    if ((char.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        if (g.setCharacteristicNotification(char, true)) {
                            val cccd = char.getDescriptor(CCCD)
                            if (cccd != null) {
                                opQueue.enqueue {
                                    writeDescriptor(g, cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                                }
                            }
                        }
                    }
                }
            }

            var svc = g.getService(INPUT_SERVICE)
            if (svc == null) {
                svc = g.getService(KEYLINKER_SERVICE)
            }

            if (svc == null) {
                val uuids = g.services.joinToString(", ") { it.uuid.toString().take(8) }
                _connectionState.value = JoyconConnectionState(
                    error = "[$deviceName] Not a compatible Joy-Con 2 (Services: $uuids)",
                    deviceName = deviceName
                )
                return
            }

            if (svc.uuid == KEYLINKER_SERVICE) {
                Log.i(TAG, "[$side] Using Keylinker service")
                writeChar = svc.getCharacteristic(KEYLINKER_WRITE)
                notifyChar = svc.getCharacteristic(KEYLINKER_NOTIFY)
                cmdResponseChar = svc.getCharacteristic(KEYLINKER_NOTIFY)
            } else {
                writeChar = svc.getCharacteristic(WRITE_CHAR)
                notifyChar = svc.getCharacteristic(NOTIFY_CHAR) ?: svc.getCharacteristic(NYXI_INPUT_NOTIFY_CHAR)
                cmdResponseChar = svc.getCharacteristic(CMD_RESPONSE_CHAR)
            }

            if (writeChar == null || notifyChar == null) {
                _connectionState.value = JoyconConnectionState(
                    error = "Missing BLE characteristics", deviceName = deviceName,
                    bondState = g.device.bondState
                )
                return
            }
            writeChar!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

            // Subscribe to command response notifications
            if (cmdResponseChar != null && cmdResponseChar != notifyChar) {
                g.setCharacteristicNotification(cmdResponseChar, true)
                val cmdCccd = cmdResponseChar!!.getDescriptor(CCCD)
                if (cmdCccd != null) {
                    opQueue.enqueue {
                        writeDescriptor(g, cmdCccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    }
                }
            }

            // Subscribe to input notifications
            g.setCharacteristicNotification(notifyChar, true)
            val notifyCccd = notifyChar!!.getDescriptor(CCCD)
            if (notifyCccd != null) {
                opQueue.enqueue {
                    writeDescriptor(g, notifyCccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                }
            }

            // Subscribe to Nyxi / Espressif input notifications if present as a distinct characteristic
            val nyxiNotifyChar = svc.getCharacteristic(NYXI_INPUT_NOTIFY_CHAR)
            if (nyxiNotifyChar != null && nyxiNotifyChar != notifyChar) {
                g.setCharacteristicNotification(nyxiNotifyChar, true)
                val nyxiCccd = nyxiNotifyChar.getDescriptor(CCCD)
                if (nyxiCccd != null) {
                    opQueue.enqueue {
                        writeDescriptor(g, nyxiCccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    }
                }
            }

            enqueueInitWrite(g, INIT_CMD_1)
            enqueueInitWrite(g, INIT_CMD_2)
            if (svc.uuid != KEYLINKER_SERVICE) {
                enqueueInitWrite(g, SPI_READ_COLOR_CMD)
            }

            opQueue.enqueue {
                initComplete = true
                _connectionState.value = _connectionState.value.copy(
                    connected = true, ready = true, deviceName = deviceName,
                    bondState = g.device.bondState
                )
                Log.i(TAG, "[$side] Init sequence complete — requesting HIGH priority")
                if (highPriority) requestPriority(g)

                // Schedule follow-up priority requests to prevent Android Bluetooth stack demotion when multiple controllers connect
                mainHandler.postDelayed({ if (initComplete && highPriority) requestPriority(g) }, 1000L)
                mainHandler.postDelayed({ if (initComplete && highPriority) requestPriority(g) }, 2500L)

                false
            }
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            mainHandler.post { opQueue.complete() }
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int
        ) {
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

    fun setHighPriority(enabled: Boolean) {
        highPriority = enabled
        if (initComplete) gatt?.let(::requestPriority)
    }

    private fun requestPriority(g: BluetoothGatt) {
        val priority = if (highPriority) {
            BluetoothGatt.CONNECTION_PRIORITY_HIGH
        } else {
            BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        }
        g.requestConnectionPriority(priority)
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
        val cmd = if (pending != null) playerLedCmd(pending.ledBitmask) else LED_ALL_ON_CMD
        return writeCharacteristic(g, writeChar!!, cmd)
    }

    private fun enqueueInitWrite(g: BluetoothGatt, bytes: ByteArray) {
        opQueue.enqueue { writeCharacteristic(g, writeChar!!, bytes) }
    }

    private fun handleCharacteristicChanged(g: BluetoothGatt, uuid: UUID, data: ByteArray) {
        val isNyxiChar = uuid == NYXI_INPUT_NOTIFY_CHAR || uuid == KEYLINKER_NOTIFY
        
        PacketParser.parse(data, side, isNyxiChar)?.let { parsed ->
            _input.value = stickCalibrator.calibrate(parsed)
            if (!ledSentAfterFirstPacket && initComplete) {
                ledSentAfterFirstPacket = true
                mainHandler.post { opQueue.enqueue { sendLedCommand(g) } }
            }
        }

        if (uuid == CMD_RESPONSE_CHAR || uuid == KEYLINKER_NOTIFY) {
            handleCmdResponse(data)
        }
    }

    private fun handleCmdResponse(data: ByteArray) {
        SpiColorParser.parseAccentColor(data)?.let { color ->
            _connectionState.value = _connectionState.value.copy(accentColor = color)
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
