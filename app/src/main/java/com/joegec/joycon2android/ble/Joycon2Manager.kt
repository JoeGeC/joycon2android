package com.joegec.joycon2android.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.joegec.joycon2android.model.Joycon2State
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Manages BLE scanning, connection, and communication with a single Joy-Con 2.
 *
 * Connection sequence (mirrors the working macOS implementation):
 * 1. Scan for manufacturer ID 0x0553 (Nintendo)
 * 2. Connect with TRANSPORT_LE
 * 3. Request MTU 247 (default 23 truncates the 63-byte packets)
 * 4. Discover services, locate write/notify characteristics
 * 5. Enable CCCD notifications on the notify characteristic
 * 6. Send two 12-byte init commands (500ms apart) to start input reports
 * 7. Parse incoming 63-byte packets into Joycon2State
 */
class Joycon2Manager(private val context: Context) {

    companion object {
        private const val TAG = "Joycon2"

        private const val NINTENDO_MANUFACTURER_ID = 0x0553

        private val INPUT_SERVICE = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd0")
        private val NOTIFY_CHAR = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd2")
        private val WRITE_CHAR = UUID.fromString("649d4ac9-8eb7-4e6c-af44-1ea54fe5f005")
        private val CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        // Init commands: enable standard reports, then IMU/extended reports
        private val INIT_CMD_1 = byteArrayOf(
            0x0C, 0x91.toByte(), 0x01, 0x02, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        )
        private val INIT_CMD_2 = byteArrayOf(
            0x0C, 0x91.toByte(), 0x01, 0x04, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        )

        private const val DESIRED_MTU = 247
        private const val INIT_GAP_MS = 500L
        private const val SCAN_TIMEOUT_MS = 15_000L
    }

    private val _state = MutableStateFlow(Joycon2State())
    val state: StateFlow<Joycon2State> = _state.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val opQueue = GattOpQueue()
    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private var notifyChar: BluetoothGattCharacteristic? = null
    private var side: Side = Side.UNKNOWN

    @Volatile
    private var scanning = false

    @SuppressLint("MissingPermission")
    fun startScan() {
        val scanner = adapter?.bluetoothLeScanner ?: run {
            _state.value = Joycon2State(error = "Bluetooth is off or unavailable")
            return
        }

        scanning = true
        _state.value = Joycon2State(scanning = true)

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(null, settings, scanCallback)
        Log.i(TAG, "Scanning for Joy-Con 2...")

        mainHandler.postDelayed({
            if (scanning) {
                scanning = false
                adapter.bluetoothLeScanner?.stopScan(scanCallback)
                _state.value = Joycon2State(
                    error = "No Joy-Con found. Press SYNC on the controller and try again."
                )
            }
        }, SCAN_TIMEOUT_MS)
    }

    fun emitError(message: String) {
        _state.value = Joycon2State(error = message)
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        scanning = false
        mainHandler.removeCallbacksAndMessages(null)
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        opQueue.clear()
        _state.value = Joycon2State()
    }

    // region Scanning

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val record: ScanRecord = result.scanRecord ?: return
            record.getManufacturerSpecificData(NINTENDO_MANUFACTURER_ID) ?: return
            if (!scanning) return
            scanning = false
            adapter?.bluetoothLeScanner?.stopScan(this)

            val name = result.device.name ?: record.deviceName ?: "Joy-Con 2"
            side = detectSide(name)
            Log.i(TAG, "Found: '$name' side=$side")
            _state.value = Joycon2State(connecting = true, side = side, foundDeviceName = name)

            gatt = result.device.connectGatt(
                context, false, gattCallback, BluetoothDevice.TRANSPORT_LE
            )
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
            scanning = false
            _state.value = Joycon2State(
                error = when (errorCode) {
                    SCAN_FAILED_ALREADY_STARTED -> "Scan already in progress"
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "BLE app registration failed"
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE scanning not supported"
                    SCAN_FAILED_INTERNAL_ERROR -> "Internal BLE error"
                    else -> "Scan failed (code $errorCode)"
                }
            )
        }
    }

    private fun detectSide(name: String): Side = when {
        name.contains("(L)") || name.contains("Left") -> Side.LEFT
        name.contains("(R)") || name.contains("Right") -> Side.RIGHT
        name.contains("Pro") -> Side.PRO
        else -> Side.UNKNOWN
    }

    // endregion

    // region GATT

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected. Requesting MTU $DESIRED_MTU")
                    _state.value = Joycon2State(connected = true, side = side)
                    g.requestMtu(DESIRED_MTU)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.w(TAG, "Disconnected (status=$status)")
                    opQueue.clear()
                    _state.value = Joycon2State(
                        side = side,
                        error = if (status != BluetoothGatt.GATT_SUCCESS) {
                            "Connection lost (status $status). Try pressing SYNC and reconnecting."
                        } else null
                    )
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG, "MTU=$mtu (status=$status). Discovering services.")
            g.discoverServices()
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Service discovery failed: $status")
                _state.value = Joycon2State(error = "Service discovery failed")
                return
            }

            val svc = g.getService(INPUT_SERVICE)
            if (svc == null) {
                Log.e(TAG, "Input service not found on device")
                _state.value = Joycon2State(error = "Not a compatible Joy-Con 2")
                return
            }

            writeChar = svc.getCharacteristic(WRITE_CHAR)
            notifyChar = svc.getCharacteristic(NOTIFY_CHAR)
            if (writeChar == null || notifyChar == null) {
                Log.e(TAG, "Required characteristics missing")
                _state.value = Joycon2State(error = "Device missing required BLE characteristics")
                return
            }
            writeChar!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

            // Sequence: enable notifications via CCCD, then send both init commands
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
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            Log.i(TAG, "CCCD write status=$status")
            opQueue.complete()
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int
        ) {
            // 500ms gap between init writes mirrors the working macOS timing
            mainHandler.postDelayed({ opQueue.complete() }, INIT_GAP_MS)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic
        ) {
            if (ch.uuid != NOTIFY_CHAR) return
            val data = ch.value ?: return
            PacketParser.parse(data, side)?.let { _state.value = it }
        }
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

    // endregion
}
