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
import com.joegec.joycon2android.model.ControllerModel

@SuppressLint("MissingPermission")
class BleScanner(context: Context) {

    companion object {
        private const val TAG = "Joycon2"
        private const val NINTENDO_MANUFACTURER_ID = 0x0553
        private const val SCAN_TIMEOUT_MS = 15_000L
    }

    var onDeviceFound: ((ScanResult, ControllerModel, String) -> Unit)? = null
    var onScanFailed: ((Int) -> Unit)? = null
    var onTimeout: (() -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    @Volatile
    var isScanning = false
        private set

    val isAvailable: Boolean get() = adapter?.bluetoothLeScanner != null

    fun start(isConnected: (String) -> Boolean) {
        if (isScanning) return
        val scanner = adapter?.bluetoothLeScanner ?: return

        isScanning = true
        scanner.startScan(null, lowLatencySettings(), createCallback(isConnected))
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

    private fun createCallback(isConnected: (String) -> Boolean): ScanCallback {
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (!isScanning) return
                val manufacturerData = nintendoData(result) ?: return
                logAdvertisement(result, manufacturerData)
                // Only the bonded host can connect to a wake advert: docs/protocol.md#advertising
                if (!JoyconAdvertisement.isPairing(manufacturerData)) return
                if (isConnected(result.device.address)) return

                val name = result.device.name
                    ?: result.scanRecord?.deviceName
                    ?: "Joy-Con 2"
                val model = modelFromName(name)
                    ?: JoyconAdvertisement.model(manufacturerData)
                    ?: ControllerModel.UNKNOWN
                onDeviceFound?.invoke(result, model, name)
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

    private fun modelFromName(name: String): ControllerModel? = when {
        name.contains("(L)") || name.contains("Left") -> ControllerModel.JOYCON_LEFT
        name.contains("(R)") || name.contains("Right") -> ControllerModel.JOYCON_RIGHT
        name.contains("Pro") -> ControllerModel.PRO_CONTROLLER
        else -> null
    }
}
