package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ConnectionViewMode

class SetViewModeUseCase(private val preferences: ViewModePreferences) {
    suspend operator fun invoke(mode: ConnectionViewMode) = preferences.setViewMode(mode)
}
