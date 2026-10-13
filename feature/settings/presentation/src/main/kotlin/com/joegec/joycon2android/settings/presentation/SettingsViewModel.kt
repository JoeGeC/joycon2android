package com.joegec.joycon2android.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.settings.ObserveOutputSettingsUseCase
import com.joegec.joycon2android.settings.OutputSettings
import com.joegec.joycon2android.settings.SetBlockDeviceMotionUseCase
import com.joegec.joycon2android.settings.SetFasterUpdatesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeOutputSettings: ObserveOutputSettingsUseCase,
    private val setFasterUpdates: SetFasterUpdatesUseCase,
    private val setBlockDeviceMotion: SetBlockDeviceMotionUseCase,
) : ViewModel() {

    val outputSettings: StateFlow<OutputSettings> = observeOutputSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), OutputSettings())

    fun toggleFasterUpdates(enabled: Boolean) {
        viewModelScope.launch { setFasterUpdates(enabled) }
    }

    fun toggleBlockDeviceMotion(enabled: Boolean) {
        viewModelScope.launch { setBlockDeviceMotion(enabled) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
