package com.joegec.joycon2android.output.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.model.EmulatorSetupResult
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.output.DeviceOverride
import com.joegec.joycon2android.output.EmulatorKind
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.ObserveOutputRunningUseCase
import com.joegec.joycon2android.output.ObserveOutputSelectionUseCase
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputMode
import com.joegec.joycon2android.output.OutputSelection
import com.joegec.joycon2android.output.SelectEmulatorUseCase
import com.joegec.joycon2android.output.SetDeviceOverrideUseCase
import com.joegec.joycon2android.output.SetOutputModeUseCase
import com.joegec.joycon2android.output.SetOutputRunningUseCase
import com.joegec.joycon2android.output.selected
import com.joegec.joycon2android.ui.components.DolphinSetupPhase
import com.joegec.joycon2android.ui.components.EmulatorOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OutputViewModel(
    observeOutputSelection: ObserveOutputSelectionUseCase,
    observeOutputRunning: ObserveOutputRunningUseCase,
    private val setOutputRunning: SetOutputRunningUseCase,
    private val setOutputMode: SetOutputModeUseCase,
    private val saveEmulatorChoice: SelectEmulatorUseCase,
    private val setDeviceOverride: SetDeviceOverrideUseCase,
    val emulators: List<EmulatorTarget>,
    private val configure: suspend (target: EmulatorTarget, players: List<PlayerState>, closeEmulator: Boolean) -> EmulatorSetupResult,
) : ViewModel() {

    val selection: StateFlow<OutputSelection> = observeOutputSelection()
        .stateIn(viewModelScope, SharingStarted.Eagerly, OutputSelection())

    val running: StateFlow<Boolean> = observeOutputRunning()

    /** The hand-set-up target whose guide is open, in place of writing a config. */
    private val _guide = MutableStateFlow<EmulatorKind?>(null)
    val guide: StateFlow<EmulatorKind?> = _guide.asStateFlow()

    private val _setupPhase = MutableStateFlow(DolphinSetupPhase.IDLE)
    val setupPhase: StateFlow<DolphinSetupPhase> = _setupPhase.asStateFlow()

    private val _emulatorToClose = MutableStateFlow<EmulatorOption?>(null)
    val emulatorToClose: StateFlow<EmulatorOption?> = _emulatorToClose.asStateFlow()

    /** Its id is the package to launch, not the target's. */
    private val _emulatorToStart = MutableStateFlow<EmulatorOption?>(null)
    val emulatorToStart: StateFlow<EmulatorOption?> = _emulatorToStart.asStateFlow()

    fun selectedEmulatorId(selection: OutputSelection): String? = emulators.selected(selection.emulatorId)?.id

    fun toggle(running: Boolean) = setOutputRunning(running)

    fun setMode(mode: OutputMode) {
        viewModelScope.launch { setOutputMode(mode) }
        resetSetupPhase()
    }

    fun selectEmulator(id: String) {
        viewModelScope.launch { saveEmulatorChoice(id) }
        _emulatorToClose.value = null
        resetSetupPhase()
    }

    fun setOverride(device: OutputDevice, override: DeviceOverride) {
        viewModelScope.launch { setDeviceOverride(device, override) }
        resetSetupPhase()
    }

    fun resetSetupPhase() {
        if (_setupPhase.value == DolphinSetupPhase.WORKING) return
        _setupPhase.value = DolphinSetupPhase.IDLE
        _emulatorToStart.value = null
    }

    fun setUp(players: List<PlayerState>) {
        val target = emulators.selected(selection.value.emulatorId) ?: return
        if (target.packageName == null) _guide.value = target.kind else write(players, closeEmulator = false)
    }

    fun dismissGuide() {
        _guide.value = null
    }

    fun closeEmulatorAndSetUp(players: List<PlayerState>) {
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
        val target = emulators.selected(selection.value.emulatorId) ?: return
        val packageName = target.packageName ?: return
        val name = target.name ?: return
        if (_setupPhase.value == DolphinSetupPhase.WORKING) return
        viewModelScope.launch {
            _setupPhase.value = DolphinSetupPhase.WORKING
            val result = try {
                configure(target, players, closeEmulator)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                EmulatorSetupResult.FAILED
            }
            if (result == EmulatorSetupResult.EMULATOR_RUNNING) {
                _emulatorToClose.value = EmulatorOption(target.id, name)
                _setupPhase.value = DolphinSetupPhase.IDLE
            } else {
                _setupPhase.value = result.toPhase()
                if (result == EmulatorSetupResult.SUCCESS) _emulatorToStart.value = EmulatorOption(packageName, name)
            }
        }
    }
}

// EMULATOR_RUNNING never reaches here — it raises the close-emulator prompt instead.
private fun EmulatorSetupResult.toPhase() = when (this) {
    EmulatorSetupResult.SUCCESS -> DolphinSetupPhase.SUCCESS
    EmulatorSetupResult.NO_PRIVILEGED_ACCESS -> DolphinSetupPhase.NO_ACCESS
    else -> DolphinSetupPhase.FAILED
}
