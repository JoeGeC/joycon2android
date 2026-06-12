package com.joegec.joycon2android.uhid

import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GamepadOutput(
    private val scope: CoroutineScope,
    private val gamepadManager: GamepadManager,
    private val acquireShell: (onResult: (PrivilegedShell?) -> Unit) -> Unit,
    private val activePlayers: () -> List<PlayerState>,
) {

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var shell: PrivilegedShell? = null

    private val playerStateFlows = PlayerNumber.entries.associateWith {
        MutableStateFlow(PlayerState(it))
    }

    fun enable() {
        if (_enabled.value) return
        acquireShell { granted ->
            if (granted != null) {
                shell = granted
                scope.launch { startOutput(granted) }
            } else {
                _error.value = "No privileged access — set up Shizuku or wireless debugging"
            }
        }
    }

    fun disable() {
        _enabled.value = false
        shell = null
        gamepadManager.destroyAll()
    }

    fun destroyAll() = gamepadManager.destroyAll()

    fun push(players: List<PlayerState>) {
        if (!_enabled.value) return
        players.forEach { playerStateFlows[it.player]?.value = it }
    }

    fun onPlayerAssigned(player: PlayerNumber) {
        if (!_enabled.value) return
        val active = shell ?: return
        scope.launch {
            if (gamepadManager.createGamepad(player, active)) {
                gamepadManager.startReporting(player, playerStateFlows.getValue(player))
            }
        }
    }

    fun onPlayerUnassigned(player: PlayerNumber) {
        if (!_enabled.value) return
        gamepadManager.destroyGamepad(player)
        if (gamepadManager.activeCount == 0) disable()
    }

    private suspend fun startOutput(shell: PrivilegedShell) {
        val active = activePlayers()
        if (active.isEmpty()) {
            _error.value = "No controllers assigned"
            return
        }

        var anyCreated = false
        for (playerState in active) {
            if (gamepadManager.createGamepad(playerState.player, shell)) {
                val flow = playerStateFlows.getValue(playerState.player)
                flow.value = playerState
                gamepadManager.startReporting(playerState.player, flow)
                anyCreated = true
            }
        }

        if (anyCreated) {
            _enabled.value = true
            _error.value = null
        } else {
            _error.value = "Failed to create virtual gamepad — check privileged access"
        }
    }
}
