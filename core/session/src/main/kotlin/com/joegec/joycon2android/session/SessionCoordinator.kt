package com.joegec.joycon2android.session

import com.joegec.joycon2android.connection.ControllerRepository
import com.joegec.joycon2android.assignment.AssignmentRepository
import com.joegec.joycon2android.assignment.ComboAssignmentDetector
import com.joegec.joycon2android.assignment.PlayerStateResolver
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * The cross-feature glue: joins connected controllers with player assignments into
 * [AppUiState], drives the per-packet output pipeline, and orchestrates assignment
 * (including its connection/gamepad side effects).
 *
 * Lives above the feature domains and depends only on their interfaces. The actual
 * gamepad/DSU effects are injected as callbacks ([onState], [onPlayerAssigned],
 * [onPlayerUnassigned]) so this module never depends on those features.
 *
 * [onState] fires synchronously on every emission — the controller list re-emits on each
 * input change, so per-packet consumers (gamepad, DSU motion) see every update.
 */
class SessionCoordinator(
    private val scope: CoroutineScope,
    private val controllers: ControllerRepository,
    private val assignments: AssignmentRepository,
    private val resolver: PlayerStateResolver,
    private val comboDetector: ComboAssignmentDetector,
    private val onState: (AppUiState) -> Unit,
    private val onPlayerAssigned: (PlayerNumber) -> Unit,
    private val onPlayerUnassigned: (PlayerNumber) -> Unit,
) {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun start() {
        scope.launch {
            combine(
                controllers.controllers,
                controllers.scanning,
                controllers.error,
                assignments.assignments,
            ) { controllerList, scanning, error, assigned ->
                evictDisappeared(controllerList, assigned)
                build(controllerList, scanning, error, assigned)
            }.collect(::publish)
        }
    }

    fun assign(address: String, player: PlayerNumber) {
        val side = controllers.controllers.value.find { it.address == address }?.side ?: return
        if (!assignments.assign(address, side, player)) return
        controllers.setPlayerLed(address, player)
        onPlayerAssigned(player)
    }

    fun unassign(address: String) {
        val player = assignments.getPlayer(address)
        assignments.unassign(address)
        controllers.setPlayerLed(address, null)
        player?.let(onPlayerUnassigned)
    }

    private fun publish(state: AppUiState) {
        _uiState.value = state
        onState(state)
        applyCombos(state.unassignedJoycons)
    }

    private fun applyCombos(unassigned: List<ConnectedJoycon>) {
        for (combo in comboDetector.detect(unassigned)) {
            val player = assignments.nextFreePlayer() ?: return
            combo.addresses.forEach { address -> assign(address, player) }
        }
    }

    // A controller that dropped off the bus shouldn't keep its player slot
    private fun evictDisappeared(controllers: List<ConnectedJoycon>, assigned: Map<String, PlayerNumber>) {
        val present = controllers.mapTo(mutableSetOf()) { it.address }
        (assigned.keys - present).forEach(assignments::unassign)
    }

    private fun build(
        controllers: List<ConnectedJoycon>,
        scanning: Boolean,
        error: String?,
        assigned: Map<String, PlayerNumber>,
    ): AppUiState {
        val joycons = controllers.map { it.copy(assignedPlayer = assigned[it.address]) }
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
