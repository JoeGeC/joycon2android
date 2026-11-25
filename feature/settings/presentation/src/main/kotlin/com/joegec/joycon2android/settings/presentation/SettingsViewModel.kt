package com.joegec.joycon2android.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.settings.LowBatteryAlert
import com.joegec.joycon2android.settings.ObserveConnectionLossNotificationUseCase
import com.joegec.joycon2android.settings.ObserveLaunchOnMainScreenUseCase
import com.joegec.joycon2android.settings.ObserveLowBatteryAlertUseCase
import com.joegec.joycon2android.settings.ObserveOutputSettingsUseCase
import com.joegec.joycon2android.settings.ObserveShowTipsUseCase
import com.joegec.joycon2android.settings.ObserveWarnUnsavedMappingUseCase
import com.joegec.joycon2android.settings.OutputSettings
import com.joegec.joycon2android.settings.SetBlockDeviceMotionUseCase
import com.joegec.joycon2android.settings.SetConnectionLossNotificationUseCase
import com.joegec.joycon2android.settings.SetFasterUpdatesUseCase
import com.joegec.joycon2android.settings.SetLaunchOnMainScreenUseCase
import com.joegec.joycon2android.settings.SetLowBatteryNotificationUseCase
import com.joegec.joycon2android.settings.SetLowBatteryVibrationUseCase
import com.joegec.joycon2android.settings.SetLowBatteryWarningsUseCase
import com.joegec.joycon2android.settings.SetShowTipsUseCase
import com.joegec.joycon2android.settings.SetWarnUnsavedMappingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeOutputSettings: ObserveOutputSettingsUseCase,
    private val setFasterUpdates: SetFasterUpdatesUseCase,
    private val setBlockDeviceMotion: SetBlockDeviceMotionUseCase,
    observeShowTips: ObserveShowTipsUseCase,
    private val setShowTips: SetShowTipsUseCase,
    observeLaunchOnMainScreen: ObserveLaunchOnMainScreenUseCase,
    private val setLaunchOnMainScreen: SetLaunchOnMainScreenUseCase,
    observeWarnUnsavedMapping: ObserveWarnUnsavedMappingUseCase,
    private val setWarnUnsavedMapping: SetWarnUnsavedMappingUseCase,
    observeLowBatteryAlert: ObserveLowBatteryAlertUseCase,
    private val setLowBatteryNotification: SetLowBatteryNotificationUseCase,
    private val setLowBatteryVibration: SetLowBatteryVibrationUseCase,
    private val setLowBatteryWarningPercents: SetLowBatteryWarningsUseCase,
    observeConnectionLossNotification: ObserveConnectionLossNotificationUseCase,
    private val setConnectionLossNotification: SetConnectionLossNotificationUseCase,
) : ViewModel() {

    val outputSettings: StateFlow<OutputSettings> = observeOutputSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), OutputSettings())

    // Off until read, so a player who turned tips off never sees them flash in.
    val showTips: StateFlow<Boolean> = observeShowTips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    // Read as .value when an emulator starts, so it can't wait for a subscriber.
    val launchOnMainScreen: StateFlow<Boolean> = observeLaunchOnMainScreen()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // On until read, so a slow read can't skip the warning.
    val warnUnsavedMapping: StateFlow<Boolean> = observeWarnUnsavedMapping()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    val lowBatteryAlert: StateFlow<LowBatteryAlert> = observeLowBatteryAlert()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LowBatteryAlert())

    fun toggleLowBatteryNotification(enabled: Boolean) {
        viewModelScope.launch { setLowBatteryNotification(enabled) }
    }

    fun toggleLowBatteryVibration(enabled: Boolean) {
        viewModelScope.launch { setLowBatteryVibration(enabled) }
    }

    fun setLowBatteryWarnings(first: Int, final: Int) {
        viewModelScope.launch { setLowBatteryWarningPercents(first, final) }
    }

    val connectionLossNotification: StateFlow<Boolean> = observeConnectionLossNotification()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    fun toggleConnectionLossNotification(enabled: Boolean) {
        viewModelScope.launch { setConnectionLossNotification(enabled) }
    }

    fun toggleWarnUnsavedMapping(enabled: Boolean) {
        viewModelScope.launch { setWarnUnsavedMapping(enabled) }
    }

    fun toggleFasterUpdates(enabled: Boolean) {
        viewModelScope.launch { setFasterUpdates(enabled) }
    }

    fun toggleBlockDeviceMotion(enabled: Boolean) {
        viewModelScope.launch { setBlockDeviceMotion(enabled) }
    }

    fun toggleShowTips(enabled: Boolean) {
        viewModelScope.launch { setShowTips(enabled) }
    }

    fun toggleLaunchOnMainScreen(enabled: Boolean) {
        viewModelScope.launch { setLaunchOnMainScreen(enabled) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
