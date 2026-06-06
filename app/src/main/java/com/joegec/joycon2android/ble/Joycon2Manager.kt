package com.joegec.joycon2android.ble

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Orchestrates BLE scanning and Joy-Con connection lifecycle.
 * Delegates scanning to [BleScanner] and connection tracking to [ConnectionPool].
 */
class Joycon2Manager(private val context: Context) {

    companion object {
        private const val MAX_CONNECTIONS = 8
    }

    private val scanner = BleScanner(context)
    private val pool = ConnectionPool(context)

    private val _connections = MutableStateFlow<Map<String, JoyconConnection>>(emptyMap())
    val connections: StateFlow<Map<String, JoyconConnection>> = _connections.asStateFlow()

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        scanner.onDeviceFound = ::onDeviceFound
        scanner.onScanFailed = ::onScanFailed
        scanner.onTimeout = ::onTimeout
        pool.onPoolChanged = { _connections.value = pool.all }
    }

    fun startScan() {
        if (scanner.isScanning) return
        if (pool.size >= MAX_CONNECTIONS) return

        if (!scanner.isAvailable) {
            _error.value = context.getString(R.string.error_bluetooth_off)
            return
        }

        _error.value = null
        scanner.start { address -> address in pool.addresses }
        _scanning.value = true
    }

    fun stopScan() {
        scanner.stop()
        _scanning.value = false
    }

    fun disconnectAll() {
        scanner.stop()
        pool.disconnectAll()
        _scanning.value = false
        _error.value = null
        _connections.value = emptyMap()
    }

    fun disconnect(address: String) {
        pool.disconnect(address)
        _connections.value = pool.all
    }

    fun getConnection(address: String): JoyconConnection? = pool.get(address)

    fun emitError(message: String) {
        _error.value = message
    }

    private fun onDeviceFound(result: ScanResult, side: Side, name: String) {
        if (pool.size >= MAX_CONNECTIONS) {
            scanner.stop()
            _scanning.value = false
            return
        }

        pool.connect(result, side, name) ?: return
        _connections.value = pool.all
    }

    private fun onScanFailed(errorCode: Int) {
        _scanning.value = false
        _error.value = scanErrorMessage(errorCode)
    }

    private fun onTimeout() {
        _scanning.value = false
        if (pool.size == 0) {
            _error.value = context.getString(R.string.error_no_joycon)
        }
    }

    private fun scanErrorMessage(errorCode: Int): String = when (errorCode) {
        ScanCallback.SCAN_FAILED_ALREADY_STARTED -> context.getString(R.string.error_scan_already_started)
        ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> context.getString(R.string.error_ble_registration_failed)
        ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> context.getString(R.string.error_ble_unsupported)
        ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> context.getString(R.string.error_ble_internal)
        else -> context.getString(R.string.error_scan_failed, errorCode)
    }
}
