package com.joegec.joycon2android.dsu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.dsu.DisableDsuUseCase
import com.joegec.joycon2android.dsu.DsuStatus
import com.joegec.joycon2android.dsu.EnableDsuUseCase
import com.joegec.joycon2android.dsu.ObserveDsuStatusUseCase
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.components.DolphinSetupPhase // shared, in :core:designsystem
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
    val dolphinInstalled: Boolean = false,
    private val configureDolphin: suspend (List<PlayerState>) -> Boolean = { false },
) : ViewModel() {

    val status: StateFlow<DsuStatus> = observeDsuStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DsuStatus())

    private val _dolphinPhase = MutableStateFlow(DolphinSetupPhase.IDLE)
    val dolphinPhase: StateFlow<DolphinSetupPhase> = _dolphinPhase.asStateFlow()

    fun toggle(enabled: Boolean) {
        if (enabled) enableDsu() else disableDsu()
    }

    /** Clears a stale Done/Failed once the written config no longer matches the assignment. */
    fun resetDolphinPhase() {
        if (_dolphinPhase.value != DolphinSetupPhase.WORKING) _dolphinPhase.value = DolphinSetupPhase.IDLE
    }

    fun configureDolphinDsu(players: List<PlayerState>) {
        if (_dolphinPhase.value == DolphinSetupPhase.WORKING) return
        viewModelScope.launch {
            _dolphinPhase.value = DolphinSetupPhase.WORKING
            _dolphinPhase.value = try {
                if (configureDolphin(players)) DolphinSetupPhase.SUCCESS else DolphinSetupPhase.FAILED
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                DolphinSetupPhase.FAILED
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
