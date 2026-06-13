package com.joegec.joycon2android.ble

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import com.joegec.joycon2android.feature.connection.data.R
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Orchestrates BLE scanning and Joy-Con connection lifecycle, exposing connected
 * controllers as domain [ConnectedJoycon]s. Delegates scanning to [BleScanner] and
 * connection tracking to [ConnectionPool], and assembles the [controllers] list from each
 * connection's live input + state (re-emitting on every change).
 */
class Joycon2Manager(
    private val context: Context,
    private val scope: CoroutineScope,
) : ControllerRepository {

    companion object {
        private const val MAX_CONNECTIONS = 8
    }

    private val scanner = BleScanner(context)
    private val pool = ConnectionPool(context)
    private val connectionJobs = mutableMapOf<String, Job>()

    private val _controllers = MutableStateFlow<List<ConnectedJoycon>>(emptyList())
    override val controllers: StateFlow<List<ConnectedJoycon>> = _controllers.asStateFlow()

    private val _scanning = MutableStateFlow(false)
    override val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    init {
        scanner.onDeviceFound = ::onDeviceFound
        scanner.onScanFailed = ::onScanFailed
        scanner.onTimeout = ::onTimeout
        pool.onPoolChanged = ::onPoolChanged
    }

    override fun startScan() {
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

    override fun stopScan() {
        scanner.stop()
        _scanning.value = false
    }

    override fun disconnectAll() {
        scanner.stop()
        pool.disconnectAll()
        _scanning.value = false
        _error.value = null
        connectionJobs.values.forEach { it.cancel() }
        connectionJobs.clear()
        _controllers.value = emptyList()
    }

    override fun disconnect(address: String) {
        pool.disconnect(address)
        onPoolChanged()
    }

    override fun setPlayerLed(address: String, player: PlayerNumber?) {
        val connection = pool.get(address) ?: return
        if (player != null) connection.setPlayerLed(player) else connection.clearPlayerLed()
    }

    override fun emitError(message: String) {
        _error.value = message
    }

    private fun onPoolChanged() {
        syncCollectors()
        rebuildControllers()
    }

    // Each connection's input + state drives a rebuild, so [controllers] reflects live data
    private fun syncCollectors() {
        (connectionJobs.keys - pool.addresses).forEach { connectionJobs.remove(it)?.cancel() }
        (pool.addresses - connectionJobs.keys).forEach { address ->
            val connection = pool.get(address) ?: return@forEach
            connectionJobs[address] = scope.launch {
                launch { connection.connectionState.collect { rebuildControllers() } }
                launch { connection.input.collect { rebuildControllers() } }
            }
        }
    }

    private fun rebuildControllers() {
        _controllers.value = pool.all.map { (address, connection) ->
            ConnectedJoycon(
                address = address,
                side = connection.side,
                deviceName = connection.deviceName,
                connectionState = connection.connectionState.value,
                input = connection.input.value,
                ready = connection.initComplete,
            )
        }
    }

    private fun onDeviceFound(result: ScanResult, side: Side, name: String) {
        if (pool.size >= MAX_CONNECTIONS) {
            scanner.stop()
            _scanning.value = false
            return
        }

        pool.connect(result, side, name) ?: return
        onPoolChanged()
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
