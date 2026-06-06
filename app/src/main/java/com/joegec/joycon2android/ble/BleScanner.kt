package com.joegec.joycon2android.ble

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
 * Handles BLE scanning for Nintendo Joy-Con 2 controllers.
 * Emits discovered devices via the [onDeviceFound] callback.
 */
class BleScanner(context: Context) {

    companion object {
        private const val TAG = "Joycon2"
        private const val NINTENDO_MANUFACTURER_ID = 0x0553
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

    @SuppressLint("MissingPermission")
    fun start(knownAddresses: Set<String>) {
        if (isScanning) return
        val scanner = adapter?.bluetoothLeScanner ?: return

        isScanning = true
        scanner.startScan(null, lowLatencySettings(), createCallback(knownAddresses))
        Log.i(TAG, "Scanning for Joy-Con 2 controllers...")
        scheduleTimeout()
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        if (!isScanning) return
        isScanning = false
        handler.removeCallbacksAndMessages(null)
        adapter?.bluetoothLeScanner?.stopScan(activeCallback)
        activeCallback = null
    }

    private var activeCallback: ScanCallback? = null

    private fun createCallback(knownAddresses: Set<String>): ScanCallback {
        val callback = object : ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (!isScanning) return
                if (!isNintendoDevice(result)) return
                if (result.device.address in knownAddresses) return

                val name = result.device.name
                    ?: result.scanRecord?.deviceName
                    ?: "Joy-Con 2"
                val side = detectSide(name)
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

    private fun isNintendoDevice(result: ScanResult): Boolean {
        val record = result.scanRecord ?: return false
        return record.getManufacturerSpecificData(NINTENDO_MANUFACTURER_ID) != null
    }

    private fun lowLatencySettings() = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private fun detectSide(name: String): Side = when {
        name.contains("(L)") || name.contains("Left") -> Side.LEFT
        name.contains("(R)") || name.contains("Right") -> Side.RIGHT
        name.contains("Pro") -> Side.PRO
        else -> Side.UNKNOWN
    }
}
