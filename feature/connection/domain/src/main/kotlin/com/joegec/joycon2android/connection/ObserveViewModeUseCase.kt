package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ConnectionViewMode
import kotlinx.coroutines.flow.Flow

class ObserveViewModeUseCase(private val preferences: ViewModePreferences) {
    operator fun invoke(): Flow<ConnectionViewMode> = preferences.viewMode
}
