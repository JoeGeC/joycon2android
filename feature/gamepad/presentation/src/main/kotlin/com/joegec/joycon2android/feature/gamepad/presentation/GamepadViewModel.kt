package com.joegec.joycon2android.feature.gamepad.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.uhid.DisableGamepadUseCase
import com.joegec.joycon2android.uhid.EnableGamepadUseCase
import com.joegec.joycon2android.uhid.GamepadStatus
import com.joegec.joycon2android.uhid.ObserveGamepadStatusUseCase
import com.joegec.joycon2android.uhid.ObserveWirelessDebugStatusUseCase
import com.joegec.joycon2android.uhid.StartPairingUseCase
import com.joegec.joycon2android.uhid.WirelessDebugStatus
import com.joegec.joycon2android.ui.components.DolphinSetupPhase
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
    val dolphinInstalled: Boolean = false,
    private val configureGamecube: suspend (List<PlayerState>) -> Boolean = { false },
) : ViewModel() {

    val status: StateFlow<GamepadStatus> = observeGamepadStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), GamepadStatus())

    val wirelessDebug: StateFlow<WirelessDebugStatus> = observeWirelessDebugStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WirelessDebugStatus())

    private val _dolphinPhase = MutableStateFlow(DolphinSetupPhase.IDLE)
    val dolphinPhase: StateFlow<DolphinSetupPhase> = _dolphinPhase.asStateFlow()

    fun toggle(enabled: Boolean, players: List<PlayerState>) {
        if (enabled) enableGamepad(players) else disableGamepad()
    }

    fun startAdbPairing() = startPairing()

    fun configureDolphinGamecube(players: List<PlayerState>) {
        if (_dolphinPhase.value == DolphinSetupPhase.WORKING) return
        viewModelScope.launch {
            _dolphinPhase.value = DolphinSetupPhase.WORKING
            _dolphinPhase.value =
                if (configureGamecube(players)) DolphinSetupPhase.SUCCESS else DolphinSetupPhase.FAILED
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
