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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private fun ByteArray.hex(): String = joinToString("") { "%02X".format(it) }

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
        val lastPackets = CopyOnWriteArrayList<String>()
        val packetCounts = ConcurrentHashMap<String, Long>()
        private const val TAG = "Joycon2"

        private val INPUT_SERVICE = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd0")
        private val NOTIFY_CHAR = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd2")
        private val WRITE_CHAR = UUID.fromString("649d4ac9-8eb7-4e6c-af44-1ea54fe5f005")
        private val CMD_RESPONSE_CHAR = UUID.fromString("c765a961-d9d8-4d36-a20a-5315b111836a")
        private val CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val KEYLINKER_SERVICE = UUID.fromString("d7f010e0-660d-46e9-96c3-19c4148bdab5")
        private val KEYLINKER_WRITE = UUID.fromString("d7f010e1-660d-46e9-96c3-19c4148bdab5")
        private val KEYLINKER_NOTIFY = UUID.fromString("d7f010e2-660d-46e9-96c3-19c4148bdab5")

        private val KEYLINKER_FF14_NOTIFY = UUID.fromString("0000ff14-0000-1000-8000-00805f9b34fb")
        private val NYXI_INPUT_NOTIFY_CHAR = UUID.fromString("d5a9e01e-2ffc-4cca-b20c-8b67142bf442")

        private val NYXI_ENABLE_HID = byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

        private val INIT_CMD_1 = byteArrayOf(
            0x0C, 0x91.toByte(), 0x01, 0x02, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        )
        private val INIT_CMD_2 = byteArrayOf(
            0x0C, 0x91.toByte(), 0x01, 0x04, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        )

        // SPI read (report 0x02, cmd 0x04): read 0x40 bytes from the DeviceInfo block
        // at 0x013000, which contains the shell colors (body color at 0x013019).
        // Payload: read length (0x40), 0x7E magic, then the 4-byte LE source address.
        // The reply arrives on the command-response characteristic and is decoded
        // by [SpiColorParser].
        // Byte [2] is 0x00 for SPI reads (matching HandHeldLegend procon2tool);
        // the INIT_CMD_* feature commands use 0x01 there, but SPI reads only
        // reply when this is 0x00.
        private val SPI_READ_COLOR_CMD = byteArrayOf(
            0x02, 0x91.toByte(), 0x00, 0x04, 0x00, 0x08, 0x00, 0x00,
            0x40, 0x7E, 0x00, 0x00, 0x00, 0x30, 0x01, 0x00
        )

        // Subcommand 0x07: set LED pattern via bitmask (16 bytes)
        // Lower nibble = solid LEDs (0x01=P1, 0x02=P2, 0x04=P3, 0x08=P4)
        // Upper nibble = flashing LEDs (0x10=P1, 0x20=P2, 0x40=P3, 0x80=P4)
        // 0xF0 = all flashing = default cycling animation
        private fun playerLedCmd(bitmask: Byte): ByteArray {
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
    private val stickCalibrator = StickCalibrator()
    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private var ff15Char: BluetoothGattCharacteristic? = null
    private var notifyChar: BluetoothGattCharacteristic? = null
    private var cmdResponseChar: BluetoothGattCharacteristic? = null
    private var pendingPlayerLed: PlayerNumber? = null
    @Volatile var initComplete = false
        private set
    @Volatile private var highPriority = false
    private var ledSentAfterFirstPacket = false

    private val isNyxiController = deviceName.contains("NJ22") || deviceName.contains("Nyxi") || deviceName.contains("Hyperion")

    fun connect(device: BluetoothDevice) {
        if (isNyxiController) {
            Log.i(TAG, "Detected Nyxi controller, applying connection workarounds.")
        }
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
            val bondState = g.device.bondState
            Log.d(TAG, "[$side] onConnectionStateChange: status=$status, newState=$newState, bondState=$bondState")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "[$side] Connected. Requesting MTU $DESIRED_MTU")
                    _connectionState.value = JoyconConnectionState(
                        connected = true, deviceName = deviceName, bondState = bondState
                    )
                    g.requestMtu(DESIRED_MTU)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.w(TAG, "[$side] Disconnected (status=$status)")

                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        val hint = when (status) {
                            133 -> "GATT_ERROR (133): Common on Android. Try toggling Bluetooth or restarting the controller."
                            8, 19, 22, 62 -> "Connection timeout/terminated. If this persists, 'Forget' the device in Android Bluetooth settings and re-pair."
                            34 -> "GATT_CONN_LMP_TIMEOUT: The controller might have stopped responding."
                            else -> "Status $status. If connection fails, ensure the controller is in pairing mode (holding SYNC)."
                        }
                        Log.w(TAG, "[$side] Connection Hint: $hint")
                    }

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
            Log.d(TAG, "[$side] onMtuChanged: mtu=$mtu, status=$status")
            Log.i(TAG, "[$side] MTU=$mtu. Discovering services.")
            if (g.device.bondState == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "[NYXI] Bonding successful, proceeding with service discovery.")
            }
            if (isNyxiController) {
                mainHandler.postDelayed({ g.discoverServices() }, 500L)
            } else {
                g.discoverServices()
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            Log.d(TAG, "[$side] onServicesDiscovered: status=$status")
            Log.i(TAG, "[$side] Services discovered (status=$status)")

            // Check for bonding state issues
            if (g.device.bondState == BluetoothDevice.BOND_BONDING) {
                Log.w(TAG, "[$side] System is attempting to bond; this may interfere with Joy-Con protocol.")
            }
            // Log device appearance/class
            Log.i(TAG, "[$side] Device BluetoothClass: ${g.device.bluetoothClass}")

            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = JoyconConnectionState(
                    error = "Service discovery failed", deviceName = deviceName
                )
                return
            }

            // Deep GATT dump: iterate through ALL discovered services and their characteristics
            for (service in g.services) {
                Log.i(TAG, "[$side] Service UUID: ${service.uuid}")
                for (characteristic in service.characteristics) {
                    val props = characteristic.properties
                    val propList = mutableListOf<String>()
                    if ((props and BluetoothGattCharacteristic.PROPERTY_READ) != 0) propList.add("Read")
                    if (((props and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) || ((props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0)) propList.add("Write")
                    if ((props and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) propList.add("Notify")
                    val propStr = if (propList.isEmpty()) "None" else propList.joinToString("/")
                    Log.i(TAG, "[$side]   Characteristic UUID: ${characteristic.uuid} ($propStr)")
                }
            }

            var svc = g.getService(INPUT_SERVICE)
            if (svc == null) {
                svc = g.getService(KEYLINKER_SERVICE)
            }

            if (svc == null) {
                val services = g.services
                val uuids = services.joinToString(", ") { it.uuid.toString().take(8) }

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

            // Subscribe to command response notifications (required for LED commands)
            if (cmdResponseChar != null && cmdResponseChar != notifyChar) {
                g.setCharacteristicNotification(cmdResponseChar, true)
                val cmdCccd = cmdResponseChar!!.getDescriptor(CCCD)
                if (cmdCccd != null) {
                    opQueue.enqueue {
                        Log.d(TAG, "[$side] Writing CMD_RESPONSE CCCD")
                        writeDescriptor(g, cmdCccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    }
                }
            }

            // Subscribe to primary input notifications
            g.setCharacteristicNotification(notifyChar, true)
            val notifyCccd = notifyChar!!.getDescriptor(CCCD)
            if (notifyCccd != null) {
                opQueue.enqueue {
                    Log.d(TAG, "[$side] Writing NOTIFY CCCD for ${notifyChar!!.uuid}")
                    writeDescriptor(g, notifyCccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                }
            }

            // Check if NYXI_INPUT_NOTIFY_CHAR also exists separately on the service and subscribe if needed
            val nyxiNotifyChar = svc.getCharacteristic(NYXI_INPUT_NOTIFY_CHAR)
            if (nyxiNotifyChar != null && nyxiNotifyChar != notifyChar) {
                g.setCharacteristicNotification(nyxiNotifyChar, true)
                val nyxiCccd = nyxiNotifyChar.getDescriptor(CCCD)
                if (nyxiCccd != null) {
                    opQueue.enqueue {
                        Log.d(TAG, "[$side] Writing NYXI NOTIFY CCCD for ${nyxiNotifyChar.uuid}")
                        writeDescriptor(g, nyxiCccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    }
                }
            }

            if (isNyxiController) {
                opQueue.enqueue {
                    Log.i(TAG, "[$side] Writing NYXI_ENABLE_HID to Keylinker write")
                    writeCharacteristic(g, writeChar!!, NYXI_ENABLE_HID)
                }
                ff15Char?.let { char ->
                    opQueue.enqueue {
                        Log.i(TAG, "[$side] Writing NYXI_ENABLE_HID to FF15")
                        writeCharacteristic(g, char, NYXI_ENABLE_HID)
                    }
                }
            }

            enqueueInitWrite(g, INIT_CMD_1)
            enqueueInitWrite(g, INIT_CMD_2)
            if (svc.uuid != KEYLINKER_SERVICE) {
                enqueueInitWrite(g, SPI_READ_COLOR_CMD)
            }

            opQueue.enqueue {
                Log.d(TAG, "[$side] Setting initComplete = true")
                initComplete = true
                _connectionState.value = _connectionState.value.copy(
                    connected = true, ready = true, deviceName = deviceName,
                    bondState = g.device.bondState
                )
                Log.i(TAG, "[$side] Init sequence complete")
                if (highPriority) requestPriority(g)
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

    fun setHighPriority(enabled: Boolean) {
        highPriority = enabled
        if (initComplete) gatt?.let(::requestPriority)
    }

    // The default "balanced" connection interval lands on 30 ms on some phones, so the Joy-Con
    // can only report ~33 times a second; high priority asks the stack for 7.5-15 ms.
    private fun requestPriority(g: BluetoothGatt) {
        val priority = if (highPriority) {
            BluetoothGatt.CONNECTION_PRIORITY_HIGH
        } else {
            BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        }
        Log.i(TAG, "[$side] Connection priority high=$highPriority accepted=${g.requestConnectionPriority(priority)}")
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
        Log.i(TAG, "[$side] Sending LED cmd: ${cmd.joinToString(" ") { "%02X".format(it) }}")
        return writeCharacteristic(g, writeChar!!, cmd)
    }

    private fun enqueueInitWrite(g: BluetoothGatt, bytes: ByteArray) {
        opQueue.enqueue { writeCharacteristic(g, writeChar!!, bytes) }
    }

    private fun handleCharacteristicChanged(g: BluetoothGatt, uuid: UUID, data: ByteArray) {
        packetCounts[deviceName] = (packetCounts[deviceName] ?: 0L) + 1L
        val prefix = data.take(4).toByteArray().hex()
        Log.d(TAG, "[$side] Notification on $uuid: len=${data.size}, data=$prefix...")

        lastPackets.add(0, data.hex())
        while (lastPackets.size > 3) {
            lastPackets.removeAt(3)
        }

        // Attempt parsing as standard Joy-Con / Switch 2 input packet
        val parsedInput = PacketParser.parse(data, side)
        if (parsedInput != null) {
            _input.value = stickCalibrator.calibrate(parsedInput)
            if (!ledSentAfterFirstPacket && initComplete) {
                ledSentAfterFirstPacket = true
                mainHandler.post { opQueue.enqueue { sendLedCommand(g) } }
            }
        }

        when (uuid) {
            NOTIFY_CHAR, KEYLINKER_NOTIFY, KEYLINKER_FF14_NOTIFY, NYXI_INPUT_NOTIFY_CHAR -> {
                if (uuid == KEYLINKER_NOTIFY || uuid == KEYLINKER_FF14_NOTIFY) {
                    handleCmdResponse(data)
                }
            }
            CMD_RESPONSE_CHAR -> {
                handleCmdResponse(data)
            }
            else -> {
                if (parsedInput == null) {
                    Log.i(TAG, "[UNKNOWN NOTIFY ${uuid}]: ${data.hex()}")
                }
            }
        }
    }

    private fun handleCmdResponse(data: ByteArray) {
        Log.d(TAG, "[$side] Cmd response: ${data.joinToString(" ") { "%02X".format(it) }}")
        Log.i(TAG, "[$side] Raw response bytes: ${data.joinToString(" ") { "%02X".format(it) }}")
        if (data.isNotEmpty() && (data[0] == 0xA1.toByte() || data[0] == 0x01.toByte())) {
            val reportData = data.drop(1).toByteArray()
            val parsed = PacketParser.parse(reportData, side) ?: PacketParser.parse(data, side)
            parsed?.let { _input.value = stickCalibrator.calibrate(it) }
            return
        }
        SpiColorParser.parseAccentColor(data)?.let { color ->
            Log.i(TAG, "[$side] Accent color: #${"%06X".format(color)}")
            _connectionState.value = _connectionState.value.copy(accentColor = color)
        }
    }

    private fun writeCharacteristic(
        g: BluetoothGatt,
        ch: BluetoothGattCharacteristic,
        value: ByteArray,
    ): Boolean {
        val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) ==
                BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            ch.value = value
            @Suppress("DEPRECATION")
            g.writeCharacteristic(ch)
        }
        Log.d(TAG, "[$side] writeCharacteristic success=$success")
        return success
    }

    private fun writeDescriptor(
        g: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        value: ByteArray,
    ): Boolean {
        val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeDescriptor(descriptor, value) == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = value
            @Suppress("DEPRECATION")
            g.writeDescriptor(descriptor)
        }
        Log.d(TAG, "[$side] writeDescriptor success=$success")
        return success
    }

}
