package com.joegec.joycon2android.domain

import com.joegec.joycon2android.ble.JoyconConnection
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * [onState] fires synchronously on every rebuild so per-packet consumers
 * (gamepad output, combo detection) never lose an emission to StateFlow conflation.
 */
class UiStateAggregator(
    private val scope: CoroutineScope,
    private val connections: StateFlow<Map<String, JoyconConnection>>,
    private val scanning: StateFlow<Boolean>,
    private val error: StateFlow<String?>,
    private val assignments: PlayerAssignmentManager,
    private val resolver: PlayerStateResolver,
    private val onState: (AppUiState) -> Unit,
) {

    private val connectionJobs = mutableMapOf<String, Job>()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun start() {
        scope.launch {
            combine(connections, scanning, error, assignments.assignments) { conns, scan, err, assigned ->
                syncInputCollectors(conns)
                build(conns, scan, err, assigned)
            }.collect { publish(it) }
        }
    }

    fun stopInputCollectors() {
        connectionJobs.values.forEach { it.cancel() }
        connectionJobs.clear()
    }

    private fun publish(state: AppUiState) {
        _uiState.value = state
        onState(state)
    }

    private fun rebuild() {
        publish(build(connections.value, scanning.value, error.value, assignments.assignments.value))
    }

    private fun syncInputCollectors(current: Map<String, JoyconConnection>) {
        (connectionJobs.keys - current.keys).forEach { address ->
            connectionJobs.remove(address)?.cancel()
            assignments.unassign(address)
        }

        (current.keys - connectionJobs.keys).forEach { address ->
            val connection = current[address] ?: return@forEach
            connectionJobs[address] = scope.launch {
                launch { connection.connectionState.collectLatest { rebuild() } }
                launch { connection.input.collectLatest { rebuild() } }
            }
        }
    }

    private fun build(
        connections: Map<String, JoyconConnection>,
        scanning: Boolean,
        error: String?,
        assigned: Map<String, PlayerNumber>,
    ): AppUiState {
        val joycons = connections.map { (address, connection) ->
            ConnectedJoycon(
                address = address,
                side = connection.side,
                deviceName = connection.deviceName,
                connectionState = connection.connectionState.value,
                input = connection.input.value,
                assignedPlayer = assigned[address],
                ready = connection.initComplete,
            )
        }

        return AppUiState(
            scanning = scanning,
            error = error,
            unassignedJoycons = joycons.filter { it.assignedPlayer == null },
            players = PlayerNumber.entries.map { player ->
                resolver.resolve(player, joycons.filter { it.assignedPlayer == player })
            },
        )
    }
}
