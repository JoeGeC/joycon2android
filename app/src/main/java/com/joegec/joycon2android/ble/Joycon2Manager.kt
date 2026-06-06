package com.joegec.joycon2android.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.joegec.joycon2android.model.ControllerState
import com.joegec.joycon2android.model.JoyconConnectionState
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Scans for and connects to up to two Joy-Con 2 controllers (left + right).
 * Continues scanning until both are found or the timeout expires.
 */
class Joycon2Manager(private val context: Context) {

    companion object {
        private const val TAG = "Joycon2"
        private const val NINTENDO_MANUFACTURER_ID = 0x0553
        private const val SCAN_TIMEOUT_MS = 15_000L
    }

    private val _state = MutableStateFlow(ControllerState())
    val state: StateFlow<ControllerState> = _state.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val adapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private var leftConnection: JoyconConnection? = null
    private var rightConnection: JoyconConnection? = null

    // Track connected device addresses to avoid connecting to the same device twice
    private val connectedAddresses = mutableSetOf<String>()

    @Volatile
    private var scanning = false

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (scanning) return
        if (leftConnection != null && rightConnection != null) return

        val scanner = adapter?.bluetoothLeScanner ?: run {
            updateState(error = "Bluetooth is off or unavailable")
            return
        }

        scanning = true
        updateState()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(null, settings, scanCallback)
        Log.i(TAG, "Scanning for Joy-Con 2 controllers...")

        mainHandler.postDelayed({
            if (scanning) {
                stopScanning()
                updateState(
                    error = if (!_state.value.anyConnected) {
                        "No Joy-Con found. Press SYNC on the controller and try again."
                    } else null
                )
            }
        }, SCAN_TIMEOUT_MS)
    }

    fun emitError(message: String) {
        updateState(error = message)
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        stopScanning()
        leftConnection?.disconnect()
        rightConnection?.disconnect()
        leftConnection = null
        rightConnection = null
        connectedAddresses.clear()
        _state.value = ControllerState()
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        scanning = false
        mainHandler.removeCallbacksAndMessages(null)
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val record: ScanRecord = result.scanRecord ?: return
            record.getManufacturerSpecificData(NINTENDO_MANUFACTURER_ID) ?: return
            if (!scanning) return

            val address = result.device.address
            if (address in connectedAddresses) return

            val name = result.device.name ?: record.deviceName ?: "Joy-Con 2"
            val side = detectSide(name)

            // Skip if we already have this side connected
            if (side == Side.LEFT && leftConnection != null) return
            if (side == Side.RIGHT && rightConnection != null) return

            Log.i(TAG, "Found: '$name' side=$side")
            connectedAddresses.add(address)

            val connection = JoyconConnection(context, side, name)
            when (side) {
                Side.LEFT -> leftConnection = connection
                Side.RIGHT -> rightConnection = connection
                else -> {
                    // Unknown/Pro — treat as left if empty, else right
                    if (leftConnection == null) leftConnection = connection
                    else rightConnection = connection
                }
            }

            connection.connect(result.device)
            updateState()

            // Stop scanning once we have both
            if (leftConnection != null && rightConnection != null) {
                stopScanning()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
            scanning = false
            updateState(
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

    /**
     * Called from the ViewModel to rebuild the combined state from both connections.
     * Exposed so the ViewModel can collect individual connection flows and call this.
     */
    fun buildState(): ControllerState {
        return ControllerState(
            scanning = scanning,
            left = leftConnection?.connectionState?.value ?: JoyconConnectionState(),
            right = rightConnection?.connectionState?.value ?: JoyconConnectionState(),
            leftInput = leftConnection?.input?.value
                ?: com.joegec.joycon2android.model.JoyconInput(),
            rightInput = rightConnection?.input?.value
                ?: com.joegec.joycon2android.model.JoyconInput(),
        )
    }

    private fun updateState(error: String? = null) {
        _state.value = buildState().copy(error = error)
    }

    fun getLeftConnection(): JoyconConnection? = leftConnection
    fun getRightConnection(): JoyconConnection? = rightConnection

    private fun detectSide(name: String): Side = when {
        name.contains("(L)") || name.contains("Left") -> Side.LEFT
        name.contains("(R)") || name.contains("Right") -> Side.RIGHT
        name.contains("Pro") -> Side.PRO
        else -> Side.UNKNOWN
    }
}
