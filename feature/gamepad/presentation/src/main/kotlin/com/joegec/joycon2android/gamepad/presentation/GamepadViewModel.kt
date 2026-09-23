package com.joegec.joycon2android.gamepad.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.model.EmulatorSetupResult
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.gamepad.DisableGamepadUseCase
import com.joegec.joycon2android.gamepad.EnableGamepadUseCase
import com.joegec.joycon2android.gamepad.GamepadStatus
import com.joegec.joycon2android.gamepad.ObserveGamepadStatusUseCase
import com.joegec.joycon2android.gamepad.ObserveShizukuAvailabilityUseCase
import com.joegec.joycon2android.ui.components.DolphinSetupPhase
import com.joegec.joycon2android.ui.components.EmulatorOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GamepadViewModel(
    observeGamepadStatus: ObserveGamepadStatusUseCase,
    observeShizukuAvailability: ObserveShizukuAvailabilityUseCase,
    private val enableGamepad: EnableGamepadUseCase,
    private val disableGamepad: DisableGamepadUseCase,
    val gamepadEmulators: List<EmulatorOption> = emptyList(),
    private val configureGamepad: suspend (emulatorId: String, players: List<PlayerState>, closeEmulator: Boolean) -> EmulatorSetupResult =
        { _, _, _ -> EmulatorSetupResult.FAILED },
) : ViewModel() {

    val status: StateFlow<GamepadStatus> = observeGamepadStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), GamepadStatus())

    val shizukuAvailable: StateFlow<Boolean> = observeShizukuAvailability()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    private val _selectedEmulator = MutableStateFlow(gamepadEmulators.firstOrNull()?.id ?: "")
    val selectedEmulator: StateFlow<String> = _selectedEmulator.asStateFlow()

    private val _setupPhase = MutableStateFlow(DolphinSetupPhase.IDLE)
    val setupPhase: StateFlow<DolphinSetupPhase> = _setupPhase.asStateFlow()

    private val _emulatorToClose = MutableStateFlow<EmulatorOption?>(null)
    val emulatorToClose: StateFlow<EmulatorOption?> = _emulatorToClose.asStateFlow()

    private val _emulatorToStart = MutableStateFlow<EmulatorOption?>(null)
    val emulatorToStart: StateFlow<EmulatorOption?> = _emulatorToStart.asStateFlow()

    fun toggle(enabled: Boolean, players: List<PlayerState>) {
        if (enabled) enableGamepad(players) else disableGamepad()
    }

    fun selectEmulator(id: String) {
        _selectedEmulator.value = id
        _emulatorToClose.value = null
        _emulatorToStart.value = null
        resetSetupPhase()
    }

    fun resetSetupPhase() {
        if (_setupPhase.value == DolphinSetupPhase.WORKING) return
        _setupPhase.value = DolphinSetupPhase.IDLE
        _emulatorToStart.value = null
    }

    fun configureGamepad(players: List<PlayerState>) = write(players, closeEmulator = false)

    fun closeEmulatorAndConfigure(players: List<PlayerState>) {
        _emulatorToClose.value = null
        write(players, closeEmulator = true)
    }

    fun cancelClose() {
        _emulatorToClose.value = null
        resetSetupPhase()
    }

    fun dismissStart() {
        _emulatorToStart.value = null
    }

    private fun write(players: List<PlayerState>, closeEmulator: Boolean) {
        val emulatorId = _selectedEmulator.value
        if (emulatorId.isEmpty() || _setupPhase.value == DolphinSetupPhase.WORKING) return
        viewModelScope.launch {
            _setupPhase.value = DolphinSetupPhase.WORKING
            val result = try {
                configureGamepad(emulatorId, players, closeEmulator)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                EmulatorSetupResult.FAILED
            }
            if (result == EmulatorSetupResult.EMULATOR_RUNNING) {
                _emulatorToClose.value = emulator(emulatorId)
                _setupPhase.value = DolphinSetupPhase.IDLE
            } else {
                _setupPhase.value = result.toPhase()
                if (result == EmulatorSetupResult.SUCCESS) _emulatorToStart.value = emulator(emulatorId)
            }
        }
    }

    private fun emulator(id: String) = gamepadEmulators.firstOrNull { it.id == id }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

// EMULATOR_RUNNING never reaches here — it raises the close-emulator prompt instead.
private fun EmulatorSetupResult.toPhase() = when (this) {
    EmulatorSetupResult.SUCCESS -> DolphinSetupPhase.SUCCESS
    EmulatorSetupResult.NO_PRIVILEGED_ACCESS -> DolphinSetupPhase.NO_ACCESS
    else -> DolphinSetupPhase.FAILED
}
