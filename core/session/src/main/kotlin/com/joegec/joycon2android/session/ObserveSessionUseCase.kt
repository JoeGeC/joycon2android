package com.joegec.joycon2android.session

import com.joegec.joycon2android.model.AppUiState
import kotlinx.coroutines.flow.StateFlow

class ObserveSessionUseCase(private val coordinator: SessionCoordinator) {
    operator fun invoke(): StateFlow<AppUiState> = coordinator.uiState
}
