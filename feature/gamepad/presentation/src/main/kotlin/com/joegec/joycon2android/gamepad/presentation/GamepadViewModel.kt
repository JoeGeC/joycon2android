package com.joegec.joycon2android.gamepad.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.gamepad.DisableGamepadUseCase
import com.joegec.joycon2android.gamepad.EnableGamepadUseCase
import com.joegec.joycon2android.gamepad.GamepadStatus
import com.joegec.joycon2android.gamepad.ObserveGamepadStatusUseCase
import com.joegec.joycon2android.gamepad.wirelessdebug.ObserveWirelessDebugStatusUseCase
import com.joegec.joycon2android.gamepad.wirelessdebug.StartPairingUseCase
import com.joegec.joycon2android.gamepad.wirelessdebug.WirelessDebugStatus
import com.joegec.joycon2android.ui.components.DolphinSetupPhase
import com.joegec.joycon2android.ui.components.EmulatorOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Feature-scoped state holder for the virtual gamepad and its privileged-access setup. */
class GamepadViewModel(
    observeGamepadStatus: ObserveGamepadStatusUseCase,
    observeWirelessDebugStatus: ObserveWirelessDebugStatusUseCase,
    private val enableGamepad: EnableGamepadUseCase,
    private val disableGamepad: DisableGamepadUseCase,
    private val startPairing: StartPairingUseCase,
    val gamepadEmulators: List<EmulatorOption> = emptyList(),
    private val configureGamepad: suspend (emulatorId: String, players: List<PlayerState>) -> Boolean = { _, _ -> false },
) : ViewModel() {

    val status: StateFlow<GamepadStatus> = observeGamepadStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), GamepadStatus())

    val wirelessDebug: StateFlow<WirelessDebugStatus> = observeWirelessDebugStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WirelessDebugStatus())

    private val _selectedEmulator = MutableStateFlow(gamepadEmulators.firstOrNull()?.id ?: "")
    val selectedEmulator: StateFlow<String> = _selectedEmulator.asStateFlow()

    private val _setupPhase = MutableStateFlow(DolphinSetupPhase.IDLE)
    val setupPhase: StateFlow<DolphinSetupPhase> = _setupPhase.asStateFlow()

    fun toggle(enabled: Boolean, players: List<PlayerState>) {
        if (enabled) enableGamepad(players) else disableGamepad()
    }

    fun startAdbPairing() = startPairing()

    fun selectEmulator(id: String) {
        _selectedEmulator.value = id
        resetSetupPhase()
    }

    /** Clears a stale Done/Failed once the written config no longer matches the assignment. */
    fun resetSetupPhase() {
        if (_setupPhase.value != DolphinSetupPhase.WORKING) _setupPhase.value = DolphinSetupPhase.IDLE
    }

    fun configureGamepad(players: List<PlayerState>) {
        val emulatorId = _selectedEmulator.value
        if (emulatorId.isEmpty() || _setupPhase.value == DolphinSetupPhase.WORKING) return
        viewModelScope.launch {
            _setupPhase.value = DolphinSetupPhase.WORKING
            _setupPhase.value =
                if (configureGamepad(emulatorId, players)) DolphinSetupPhase.SUCCESS else DolphinSetupPhase.FAILED
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
