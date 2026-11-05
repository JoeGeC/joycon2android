package com.joegec.joycon2android.output.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.model.EmulatorSetupResult
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.ObserveOutputOnUseCase
import com.joegec.joycon2android.output.ObservePlayingUseCase
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.output.PlayingSelection
import com.joegec.joycon2android.output.SelectEmulatorUseCase
import com.joegec.joycon2android.output.SetMotionUseCase
import com.joegec.joycon2android.output.SetOutputOnUseCase
import com.joegec.joycon2android.output.SetPlayingUseCase
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
    observePlaying: ObservePlayingUseCase,
    observeOutputOn: ObserveOutputOnUseCase,
    private val setOutputOn: SetOutputOnUseCase,
    private val savePlaying: SetPlayingUseCase,
    private val saveEmulatorChoice: SelectEmulatorUseCase,
    private val saveMotion: SetMotionUseCase,
    val emulators: Map<Playing, List<EmulatorTarget>>,
    private val configure: suspend (target: EmulatorTarget, players: List<PlayerState>, closeEmulator: Boolean) -> EmulatorSetupResult,
) : ViewModel() {

    val selection: StateFlow<PlayingSelection> = observePlaying()
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlayingSelection())

    val on: StateFlow<Boolean> = observeOutputOn()

    private val _setupPhase = MutableStateFlow(DolphinSetupPhase.IDLE)
    val setupPhase: StateFlow<DolphinSetupPhase> = _setupPhase.asStateFlow()

    private val _emulatorToClose = MutableStateFlow<EmulatorOption?>(null)
    val emulatorToClose: StateFlow<EmulatorOption?> = _emulatorToClose.asStateFlow()

    /** Its id is the package to launch, not the target's. */
    private val _emulatorToStart = MutableStateFlow<EmulatorOption?>(null)
    val emulatorToStart: StateFlow<EmulatorOption?> = _emulatorToStart.asStateFlow()

    private fun selectedIn(selection: PlayingSelection) = emulators[selection.playing].orEmpty().selected(selection.emulatorId)

    fun setOn(on: Boolean) {
        viewModelScope.launch { setOutputOn(on) }
    }

    fun setPlaying(playing: Playing) {
        viewModelScope.launch { savePlaying(playing) }
        _emulatorToClose.value = null
        resetSetupPhase()
    }

    fun selectEmulator(id: String) {
        val playing = selection.value.playing
        viewModelScope.launch { saveEmulatorChoice(playing, id) }
        _emulatorToClose.value = null
        resetSetupPhase()
    }

    fun setMotion(on: Boolean) {
        viewModelScope.launch { saveMotion(on) }
        resetSetupPhase()
    }

    fun resetSetupPhase() {
        if (_setupPhase.value == DolphinSetupPhase.WORKING) return
        _setupPhase.value = DolphinSetupPhase.IDLE
        _emulatorToStart.value = null
    }

    fun setUp(players: List<PlayerState>) = write(players, closeEmulator = false)

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
        val target = selectedIn(selection.value) ?: return
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
