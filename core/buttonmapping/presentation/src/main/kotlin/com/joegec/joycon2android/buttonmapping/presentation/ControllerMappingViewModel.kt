package com.joegec.joycon2android.buttonmapping.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.ObserveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ResetControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.SetControllerMappingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Feature-scoped state holder for the controller mapping editor screen. */
class ControllerMappingViewModel(
    private val observeControllerMapping: ObserveControllerMappingUseCase,
    private val setControllerMapping: SetControllerMappingUseCase,
    private val resetControllerMapping: ResetControllerMappingUseCase,
) : ViewModel() {

    private val mappingFlows = mutableMapOf<Pair<Console, JoyconSide>, StateFlow<Map<String, String>>>()

    fun mapping(console: Console, side: JoyconSide): StateFlow<Map<String, String>> =
        mappingFlows.getOrPut(console to side) {
            observeControllerMapping(console, side)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())
        }

    fun setMapping(console: Console, side: JoyconSide, targetKey: String, sourceId: String) {
        viewModelScope.launch { setControllerMapping(console, side, targetKey, sourceId) }
    }

    fun resetMapping(console: Console, side: JoyconSide) {
        viewModelScope.launch { resetControllerMapping(console, side) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
