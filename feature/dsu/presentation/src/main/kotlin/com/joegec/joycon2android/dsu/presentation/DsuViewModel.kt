package com.joegec.joycon2android.dsu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.dsu.DisableDsuUseCase
import com.joegec.joycon2android.dsu.DsuStatus
import com.joegec.joycon2android.dsu.EnableDsuUseCase
import com.joegec.joycon2android.dsu.ObserveDsuStatusUseCase
import com.joegec.joycon2android.dsu.motion.DsuMotionSettings
import com.joegec.joycon2android.dsu.motion.ObserveDsuMotionSettingsUseCase
import com.joegec.joycon2android.dsu.motion.SetBlockDeviceMotionUseCase
import com.joegec.joycon2android.dsu.motion.SetFastMotionUseCase
import com.joegec.joycon2android.model.EmulatorSetupResult
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.components.DolphinSetupPhase // shared, in :core:designsystem
import com.joegec.joycon2android.ui.components.EmulatorOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Feature-scoped state holder for the DSU card. Use cases are injected by the app. */
class DsuViewModel(
    observeDsuStatus: ObserveDsuStatusUseCase,
    private val enableDsu: EnableDsuUseCase,
    private val disableDsu: DisableDsuUseCase,
    observeMotionSettings: ObserveDsuMotionSettingsUseCase,
    private val setFastMotion: SetFastMotionUseCase,
    private val setBlockDeviceMotion: SetBlockDeviceMotionUseCase,
    val dsuEmulators: List<EmulatorOption> = emptyList(),
    private val configureDsu: suspend (emulatorId: String, players: List<PlayerState>, closeEmulator: Boolean) -> EmulatorSetupResult =
        { _, _, _ -> EmulatorSetupResult.FAILED },
) : ViewModel() {

    val status: StateFlow<DsuStatus> = observeDsuStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DsuStatus())

    val motionSettings: StateFlow<DsuMotionSettings> = observeMotionSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DsuMotionSettings())

    private val _selectedEmulator = MutableStateFlow(dsuEmulators.firstOrNull()?.id ?: "")
    val selectedEmulator: StateFlow<String> = _selectedEmulator.asStateFlow()

    private val _setupPhase = MutableStateFlow(DolphinSetupPhase.IDLE)
    val setupPhase: StateFlow<DolphinSetupPhase> = _setupPhase.asStateFlow()

    /** The emulator that has to be closed before its config can be written, once the user agrees. */
    private val _emulatorToClose = MutableStateFlow<EmulatorOption?>(null)
    val emulatorToClose: StateFlow<EmulatorOption?> = _emulatorToClose.asStateFlow()

    /** The emulator to offer to start once its config has been written. */
    private val _emulatorToStart = MutableStateFlow<EmulatorOption?>(null)
    val emulatorToStart: StateFlow<EmulatorOption?> = _emulatorToStart.asStateFlow()

    fun toggle(enabled: Boolean) {
        if (enabled) enableDsu() else disableDsu()
    }

    fun toggleFastMotion(enabled: Boolean) {
        viewModelScope.launch { setFastMotion(enabled) }
    }

    fun toggleBlockDeviceMotion(enabled: Boolean) {
        viewModelScope.launch { setBlockDeviceMotion(enabled) }
    }

    fun selectEmulator(id: String) {
        _selectedEmulator.value = id
        _emulatorToClose.value = null
        _emulatorToStart.value = null
        resetSetupPhase()
    }

    /** Clears a stale Done/Failed and its start prompt once the written config no longer matches the assignment. */
    fun resetSetupPhase() {
        if (_setupPhase.value == DolphinSetupPhase.WORKING) return
        _setupPhase.value = DolphinSetupPhase.IDLE
        _emulatorToStart.value = null
    }

    fun configureDsu(players: List<PlayerState>) = write(players, closeEmulator = false)

    /** The user accepted losing unsaved progress, so stop the emulator and write. */
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
                configureDsu(emulatorId, players, closeEmulator)
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

    private fun emulator(id: String) = dsuEmulators.firstOrNull { it.id == id }

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
