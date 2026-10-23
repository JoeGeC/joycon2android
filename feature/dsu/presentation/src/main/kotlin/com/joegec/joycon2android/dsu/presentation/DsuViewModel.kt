package com.joegec.joycon2android.dsu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.dsu.DsuStatus
import com.joegec.joycon2android.dsu.ObserveDsuStatusUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DsuViewModel(observeDsuStatus: ObserveDsuStatusUseCase) : ViewModel() {

    val status: StateFlow<DsuStatus> = observeDsuStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DsuStatus())

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
