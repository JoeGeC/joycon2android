package com.joegec.joycon2android.buttonmapping.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.buttonmapping.ApplyMappingPresetUseCase
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.ObserveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveMappingPresetUseCase
import com.joegec.joycon2android.buttonmapping.ObserveSidewaysRemoteUseCase
import com.joegec.joycon2android.buttonmapping.ResetControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.SetControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.SetSidewaysRemoteUseCase
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Feature-scoped state holder for the controller mapping editor screen. */
class ControllerMappingViewModel(
    private val observeControllerMapping: ObserveControllerMappingUseCase,
    private val setControllerMapping: SetControllerMappingUseCase,
    private val resetControllerMapping: ResetControllerMappingUseCase,
    private val observeMappingPreset: ObserveMappingPresetUseCase,
    private val applyMappingPreset: ApplyMappingPresetUseCase,
    private val observeSidewaysRemote: ObserveSidewaysRemoteUseCase,
    private val setSidewaysRemote: SetSidewaysRemoteUseCase,
) : ViewModel() {

    private val mappingFlows = mutableMapOf<Pair<Console, JoyconSide>, StateFlow<Map<String, String>>>()
    private val presetFlows = mutableMapOf<Console, StateFlow<String>>()
    private val sidewaysRemoteFlows = mutableMapOf<Console, StateFlow<Boolean>>()

    fun mapping(console: Console, side: JoyconSide): StateFlow<Map<String, String>> =
        mappingFlows.getOrPut(console to side) {
            observeControllerMapping(console, side)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())
        }

    fun preset(console: Console): StateFlow<String> =
        presetFlows.getOrPut(console) {
            observeMappingPreset(console)
                .map { it.id }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                    MappingPresets.default(console).id,
                )
        }

    fun sidewaysRemote(console: Console): StateFlow<Boolean> =
        sidewaysRemoteFlows.getOrPut(console) {
            observeSidewaysRemote(console)
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                    MappingPresets.default(console).sidewaysRemote,
                )
        }

    fun setSidewaysRemoteEnabled(console: Console, enabled: Boolean) {
        viewModelScope.launch { setSidewaysRemote(console, enabled) }
    }

    fun setMapping(console: Console, side: JoyconSide, targetKey: String, sourceId: String) {
        viewModelScope.launch { setControllerMapping(console, side, targetKey, sourceId) }
    }

    fun selectPreset(console: Console, presetId: String) {
        viewModelScope.launch { applyMappingPreset(console, presetId) }
    }

    fun resetMapping(console: Console, side: JoyconSide) {
        viewModelScope.launch { resetControllerMapping(console, side) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
