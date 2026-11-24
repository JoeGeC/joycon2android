package com.joegec.joycon2android.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.settings.MarkNotificationOnboardingSeenUseCase
import com.joegec.joycon2android.settings.ObserveNotificationOnboardingSeenUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationOnboardingViewModel(
    observeSeen: ObserveNotificationOnboardingSeenUseCase,
    private val markSeen: MarkNotificationOnboardingSeenUseCase,
) : ViewModel() {

    // Null until read, so a player who has seen it never sees it flash in.
    val seen: StateFlow<Boolean?> = observeSeen()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun dismiss() {
        viewModelScope.launch { markSeen() }
    }
}
