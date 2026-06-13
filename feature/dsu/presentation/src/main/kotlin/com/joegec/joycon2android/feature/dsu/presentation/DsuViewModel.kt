package com.joegec.joycon2android.feature.dsu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.dsu.DisableDsuUseCase
import com.joegec.joycon2android.dsu.DsuStatus
import com.joegec.joycon2android.dsu.EnableDsuUseCase
import com.joegec.joycon2android.dsu.ObserveDsuStatusUseCase
import com.joegec.joycon2android.dsu.SetDsuLanUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Feature-scoped state holder for the DSU card. Use cases are injected by the app. */
class DsuViewModel(
    observeDsuStatus: ObserveDsuStatusUseCase,
    private val enableDsu: EnableDsuUseCase,
    private val disableDsu: DisableDsuUseCase,
    private val setDsuLan: SetDsuLanUseCase,
) : ViewModel() {

    val status: StateFlow<DsuStatus> = observeDsuStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DsuStatus())

    fun toggle(enabled: Boolean) {
        if (enabled) enableDsu() else disableDsu()
    }

    fun setLan(enabled: Boolean) = setDsuLan(enabled)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
