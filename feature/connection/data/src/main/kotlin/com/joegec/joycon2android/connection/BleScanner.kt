package com.joegec.joycon2android.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.joegec.joycon2android.model.Side

/**
 * Handles BLE scanning for Nintendo Joy-Con 2 and compatible controllers (like Nyxi).
 * Emits discovered devices via the [onDeviceFound] callback.
 */
@SuppressLint("MissingPermission")
class BleScanner(context: Context) {

    companion object {
        private const val TAG = "Joycon2"
        private const val NINTENDO_MANUFACTURER_ID = 0x0553
        private const val SIDE_TYPE_INDEX = 5
        private const val SCAN_TIMEOUT_MS = 15_000L
    }

    var onDeviceFound: ((ScanResult, Side, String) -> Unit)? = null
    var onScanFailed: ((Int) -> Unit)? = null
    var onTimeout: (() -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    @Volatile
    var isScanning = false
        private set

    val isAvailable: Boolean get() = adapter?.bluetoothLeScanner != null

    fun start(isKnownAddress: (String) -> Boolean) {
        if (isScanning) return
        val scanner = adapter?.bluetoothLeScanner ?: return

        isScanning = true
        scanner.startScan(null, lowLatencySettings(), createCallback(isKnownAddress))
        Log.i(TAG, "Scanning for Joy-Con 2/Compatible controllers...")
        scheduleTimeout()
    }

    fun stop() {
        if (!isScanning) return
        isScanning = false
        handler.removeCallbacksAndMessages(null)
        adapter?.bluetoothLeScanner?.stopScan(activeCallback)
        activeCallback = null
    }

    private var activeCallback: ScanCallback? = null

    private fun createCallback(isKnownAddress: (String) -> Boolean): ScanCallback {
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (!isScanning) return

                val scanRecord = result.scanRecord ?: return

                // Accept devices carrying the Joy-Con 2 manufacturer record (0x0553)
                if (scanRecord.getManufacturerSpecificData(NINTENDO_MANUFACTURER_ID) == null) return

                if (isKnownAddress(result.device.address)) {
                    Log.d(TAG, "Filtered out: Already known address ${result.device.address}")
                    return
                }

                val name = result.device.name
                    ?: scanRecord.deviceName
                    ?: "Joy-Con 2"

                // Pass the data and the type to detect side correctly
                val side = detectSide(result, name)
                onDeviceFound?.invoke(result, side, name)
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "Scan failed: $errorCode")
                isScanning = false
                onScanFailed?.invoke(errorCode)
            }
        }
        activeCallback = callback
        return callback
    }

    private fun scheduleTimeout() {
        handler.postDelayed({
            if (!isScanning) return@postDelayed
            stop()
            onTimeout?.invoke()
        }, SCAN_TIMEOUT_MS)
    }

    private fun lowLatencySettings() = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private fun detectSide(result: ScanResult, name: String): Side {
        // 1. Try name-based detection first (most reliable for third-party)
        sideFromName(name)?.let { return it }

        // 2. Try manufacturer-based detection
        sideFromManufacturerData(result)?.let { return it }

        return Side.UNKNOWN
    }

    private fun sideFromName(name: String): Side? = when {
        // Matches "Joy-Con (L)", "NJ22-L", "Left Hyperion"
        name.contains("(L)") || name.contains("Left") || name.contains("-L") -> Side.LEFT
        // Matches "Joy-Con (R)", "NJ22-R", "Right Hyperion"
        name.contains("(R)") || name.contains("Right") || name.contains("-R") -> Side.RIGHT
        // Matches "Pro Controller", "NJ22"
        name.contains("Pro") || name.contains("NJ22") -> Side.PRO
        else -> null
    }

    /**
     * Extracts side info from Nintendo-specific data packets.
     * Note: Nyxi packets are usually too short for this index, so we skip this for Nyxi.
     */
    private fun sideFromManufacturerData(result: ScanResult): Side? {
        val mfgData = result.scanRecord
            ?.getManufacturerSpecificData(NINTENDO_MANUFACTURER_ID) ?: return null

        if (mfgData.size <= SIDE_TYPE_INDEX) return null

        return when (mfgData[SIDE_TYPE_INDEX].toInt() and 0xFF) {
            0x67 -> Side.LEFT
            0x66 -> Side.RIGHT
            0x69 -> Side.PRO
            else -> null
        }
    }
}