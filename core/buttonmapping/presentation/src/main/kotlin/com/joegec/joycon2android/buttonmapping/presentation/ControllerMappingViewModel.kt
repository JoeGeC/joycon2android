package com.joegec.joycon2android.buttonmapping.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.buttonmapping.ApplyGlobalLayoutUseCase
import com.joegec.joycon2android.buttonmapping.ApplyMappingLayoutUseCase
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.DeleteCustomLayoutUseCase
import com.joegec.joycon2android.buttonmapping.DeleteGlobalLayoutUseCase
import com.joegec.joycon2android.buttonmapping.ObserveGlobalMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveSavedLayoutsUseCase
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.SaveCustomLayoutUseCase
import com.joegec.joycon2android.buttonmapping.SaveGlobalLayoutUseCase
import com.joegec.joycon2android.buttonmapping.SetControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.SetSidewaysRemoteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ControllerMappingViewModel(
    private val observeGlobalMapping: ObserveGlobalMappingUseCase,
    private val observeSavedLayouts: ObserveSavedLayoutsUseCase,
    private val applyMappingLayout: ApplyMappingLayoutUseCase,
    private val applyGlobalLayout: ApplyGlobalLayoutUseCase,
    private val setControllerMapping: SetControllerMappingUseCase,
    private val setSidewaysRemote: SetSidewaysRemoteUseCase,
    private val saveCustomLayout: SaveCustomLayoutUseCase,
    private val saveGlobalLayout: SaveGlobalLayoutUseCase,
    private val deleteCustomLayout: DeleteCustomLayoutUseCase,
    private val deleteGlobalLayout: DeleteGlobalLayoutUseCase,
) : ViewModel() {

    private val editing = MutableStateFlow<MappingTarget?>(null)

    val uiState: StateFlow<ControllerMappingUiState?> = editing
        .flatMapLatest { target -> target?.let(::observe) ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun edit(console: Console, bodies: List<PlayerBody>) {
        editing.value = MappingTarget(console, bodies)
    }

    fun selectLayout(body: PlayerBody, layoutId: String) = onTarget {
        applyMappingLayout(it.console, body, layoutId)
    }

    fun selectGlobalLayout(layoutId: String) = onTarget {
        applyGlobalLayout(it.console, it.bodies, layoutId)
    }

    fun setMapping(body: PlayerBody, targetKey: String, sourceId: String) = onTarget {
        setControllerMapping(it.console, body, targetKey, sourceId)
    }

    fun setSidewaysRemoteEnabled(body: PlayerBody, enabled: Boolean) = onTarget {
        setSidewaysRemote(it.console, body, enabled)
    }

    /** Null means the whole session. */
    fun saveLayout(body: PlayerBody?, name: String) = onTarget { target ->
        if (body == null) saveGlobalLayout(target.console, target.bodies, name)
        else saveCustomLayout(target.console, body, name)
    }

    fun deleteLayout(layoutId: String, global: Boolean) = onTarget {
        if (global) deleteGlobalLayout(layoutId) else deleteCustomLayout(layoutId)
    }

    private fun observe(target: MappingTarget): Flow<ControllerMappingUiState> = combine(
        observeGlobalMapping(target.console, target.bodies),
        observeSavedLayouts(target.console),
    ) { mapping, saved -> controllerMappingUiState(target.console, mapping, saved) }

    private fun onTarget(block: suspend (MappingTarget) -> Unit) {
        val target = editing.value ?: return
        viewModelScope.launch { block(target) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private data class MappingTarget(val console: Console, val bodies: List<PlayerBody>)
