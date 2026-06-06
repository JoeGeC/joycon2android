package com.joegec.joycon2android.ble

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.ControllerState
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Orchestrates BLE scanning and Joy-Con connection management.
 * Delegates scanning to [BleScanner] and connection tracking to [ConnectionRegistry].
 */
class Joycon2Manager(private val context: Context) {

    private val scanner = BleScanner(context)
    private val registry = ConnectionRegistry(context)

    private val _state = MutableStateFlow(ControllerState())
    val state: StateFlow<ControllerState> = _state.asStateFlow()

    val isScanning: Boolean get() = scanner.isScanning

    fun getLeftConnection(): JoyconConnection? = registry.left
    fun getRightConnection(): JoyconConnection? = registry.right

    init {
        scanner.onDeviceFound = ::onDeviceFound
        scanner.onScanFailed = ::onScanFailed
        scanner.onTimeout = ::onTimeout
    }

    fun startScan() {
        if (scanner.isScanning || registry.bothConnected) return

        if (!scanner.isAvailable) {
            emitError(context.getString(R.string.error_bluetooth_off))
            return
        }

        scanner.start(registry.knownAddresses)
        updateState()
    }

    fun stop() {
        scanner.stop()
        registry.disconnectAll()
        _state.value = ControllerState()
    }

    fun emitError(message: String) {
        updateState(error = message)
    }

    fun buildState(): ControllerState = registry.buildState(scanner.isScanning)

    private fun onDeviceFound(result: ScanResult, side: Side, name: String) {
        if (registry.hasSide(side)) return

        registry.connect(result, side, name)
        updateState()

        if (registry.bothConnected) scanner.stop()
    }

    private fun onScanFailed(errorCode: Int) {
        updateState(error = scanErrorMessage(errorCode))
    }

    private fun onTimeout() {
        val error = if (!_state.value.anyConnected) {
            context.getString(R.string.error_no_joycon)
        } else null
        updateState(error = error)
    }

    private fun updateState(error: String? = null) {
        _state.value = buildState().copy(error = error)
    }

    private fun scanErrorMessage(errorCode: Int): String = when (errorCode) {
        ScanCallback.SCAN_FAILED_ALREADY_STARTED -> context.getString(R.string.error_scan_already_started)
        ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> context.getString(R.string.error_ble_registration_failed)
        ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> context.getString(R.string.error_ble_unsupported)
        ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> context.getString(R.string.error_ble_internal)
        else -> context.getString(R.string.error_scan_failed, errorCode)
    }
}
