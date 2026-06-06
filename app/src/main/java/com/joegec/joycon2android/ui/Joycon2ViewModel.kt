package com.joegec.joycon2android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.R
import com.joegec.joycon2android.ble.Joycon2Manager
import com.joegec.joycon2android.ble.JoyconConnection
import com.joegec.joycon2android.domain.PlayerAssignmentManager
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class Joycon2ViewModel(application: Application) : AndroidViewModel(application) {

    private val manager = Joycon2Manager(application)
    private val assignmentManager = PlayerAssignmentManager()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val connectionJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            combine(
                manager.connections,
                manager.scanning,
                manager.error,
                assignmentManager.assignments,
            ) { connections, scanning, error, assignments ->
                manageFlowCollectors(connections)
                buildUiState(connections, scanning, error, assignments)
            }.collect { _uiState.value = it }
        }
    }

    fun startScan() = manager.startScan()
    fun stopScan() = manager.stopScan()

    fun disconnectAll() {
        assignmentManager.unassignAll()
        cancelAllCollectors()
        manager.disconnectAll()
    }

    fun assignToPlayer(address: String, player: PlayerNumber) {
        val connection = manager.getConnection(address) ?: return
        if (!assignmentManager.assign(address, connection.side, player)) return
        connection.setPlayerLed(player)
    }

    fun unassign(address: String) {
        assignmentManager.unassign(address)
        manager.getConnection(address)?.clearPlayerLed()
    }

    fun onPermissionsDenied() {
        manager.emitError(getApplication<Application>().getString(R.string.error_permissions_denied))
    }

    override fun onCleared() {
        super.onCleared()
        manager.disconnectAll()
    }

    private fun manageFlowCollectors(connections: Map<String, JoyconConnection>) {
        val currentAddresses = connections.keys

        // Cancel collectors and unassign disconnected devices
        val removed = connectionJobs.keys - currentAddresses
        removed.forEach { address ->
            connectionJobs.remove(address)?.cancel()
            assignmentManager.unassign(address)
        }

        // Start collectors for new devices
        val added = currentAddresses - connectionJobs.keys
        added.forEach { address ->
            val connection = connections[address] ?: return@forEach
            connectionJobs[address] = viewModelScope.launch {
                launch { connection.connectionState.collectLatest { rebuildState() } }
                launch { connection.input.collectLatest { rebuildState() } }
            }
        }
    }

    private fun rebuildState() {
        _uiState.value = buildUiState(
            manager.connections.value,
            manager.scanning.value,
            manager.error.value,
            assignmentManager.assignments.value,
        )
    }

    private fun buildUiState(
        connections: Map<String, JoyconConnection>,
        scanning: Boolean,
        error: String?,
        assignments: Map<String, PlayerNumber>,
    ): AppUiState {
        val joycons = connections.map { (address, connection) ->
            ConnectedJoycon(
                address = address,
                side = connection.side,
                deviceName = connection.deviceName,
                connectionState = connection.connectionState.value,
                input = connection.input.value,
                assignedPlayer = assignments[address],
                ready = connection.initComplete,
            )
        }

        val unassigned = joycons.filter { it.assignedPlayer == null }
        val players = PlayerNumber.entries.map { player ->
            val assigned = joycons.filter { it.assignedPlayer == player }
            PlayerState(
                player = player,
                left = assigned.find { it.side == Side.LEFT },
                right = assigned.find { it.side == Side.RIGHT || it.side == Side.PRO || it.side == Side.UNKNOWN },
            )
        }

        return AppUiState(
            scanning = scanning,
            error = error,
            unassignedJoycons = unassigned,
            players = players,
        )
    }

    private fun cancelAllCollectors() {
        connectionJobs.values.forEach { it.cancel() }
        connectionJobs.clear()
    }
}
