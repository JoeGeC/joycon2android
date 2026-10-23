package com.joegec.joycon2android.gamepad.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.gamepad.GamepadStatus
import com.joegec.joycon2android.gamepad.ObserveGamepadStatusUseCase
import com.joegec.joycon2android.gamepad.ObserveShizukuAvailabilityUseCase
import com.joegec.joycon2android.gamepad.keyboard.ObserveKeyboardEnabledUseCase
import com.joegec.joycon2android.gamepad.mouse.ObserveMouseEnabledUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** The virtual devices' live state; which run is the output's choice. */
class GamepadViewModel(
    observeGamepadStatus: ObserveGamepadStatusUseCase,
    observeShizukuAvailability: ObserveShizukuAvailabilityUseCase,
    observeKeyboardEnabled: ObserveKeyboardEnabledUseCase,
    observeMouseEnabled: ObserveMouseEnabledUseCase,
) : ViewModel() {

    val status: StateFlow<GamepadStatus> = observeGamepadStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), GamepadStatus())

    val shizukuAvailable: StateFlow<Boolean> = observeShizukuAvailability()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    val keyboardEnabled: StateFlow<Boolean> = observeKeyboardEnabled()

    val mouseEnabled: StateFlow<Boolean> = observeMouseEnabled()

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
