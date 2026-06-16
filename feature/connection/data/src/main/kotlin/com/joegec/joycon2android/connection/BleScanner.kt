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
 * Handles BLE scanning for Nintendo Joy-Con 2 controllers.
 * Emits discovered devices via the [onDeviceFound] callback.
 *
 * All BLE operations require BLUETOOTH_SCAN and BLUETOOTH_CONNECT permissions,
 * which are verified by the permission launcher in MainActivity before any BLE code is reached.
 */
@SuppressLint("MissingPermission")
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

    fun start(isKnownAddress: (String) -> Boolean) {
        if (isScanning) return
        val scanner = adapter?.bluetoothLeScanner ?: return

        isScanning = true
        scanner.startScan(null, lowLatencySettings(), createCallback(isKnownAddress))
        Log.i(TAG, "Scanning for Joy-Con 2 controllers...")
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
                val manufacturerData = nintendoData(result) ?: return
                logAdvertisement(result, manufacturerData)
                // A button press wakes a synced Joy-Con into a short-lived reconnect
                // advertisement that only its bonded host can connect to (foreign
                // connects fail with status 133) — connecting just flashes the UI
                if (!JoyconAdvertisement.isPairing(manufacturerData)) return
                if (isKnownAddress(result.device.address)) return

                val name = result.device.name
                    ?: result.scanRecord?.deviceName
                    ?: "Joy-Con 2"
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

    private fun nintendoData(result: ScanResult): ByteArray? =
        result.scanRecord?.getManufacturerSpecificData(NINTENDO_MANUFACTURER_ID)

    private fun logAdvertisement(result: ScanResult, data: ByteArray) {
        Log.d(
            TAG,
            "Adv ${result.device.address} name=${result.device.name ?: result.scanRecord?.deviceName} " +
                "mfg=${data.joinToString(" ") { "%02X".format(it) }}",
        )
    }

    private fun lowLatencySettings() = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private fun detectSide(result: ScanResult, name: String): Side {
        sideFromName(name)?.let { return it }
        sideFromManufacturerData(result)?.let { return it }
        return Side.UNKNOWN
    }

    private fun sideFromName(name: String): Side? = when {
        name.contains("(L)") || name.contains("Left") -> Side.LEFT
        name.contains("(R)") || name.contains("Right") -> Side.RIGHT
        name.contains("Pro") -> Side.PRO
        else -> null
    }

    /**
     * Nintendo manufacturer data (0x0553) typically contains a device type byte.
     * Known values: 0x2D = Left Joy-Con 2, 0x2E = Right Joy-Con 2.
     */
    private fun sideFromManufacturerData(result: ScanResult): Side? {
        val mfgData = result.scanRecord
            ?.getManufacturerSpecificData(NINTENDO_MANUFACTURER_ID) ?: return null
        if (mfgData.isEmpty()) return null
        Log.d(TAG, "Manufacturer data: ${mfgData.joinToString(" ") { "%02X".format(it) }}")
        val typeByte = mfgData[0].toInt() and 0xFF
        return when (typeByte) {
            0x2D -> Side.LEFT
            0x2E -> Side.RIGHT
            else -> null
        }
    }
}
