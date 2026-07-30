package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ConnectionViewMode
import kotlinx.coroutines.flow.Flow

/** The connection view mode the user last chose, persisted across app restarts. */
interface ViewModePreferences {
    val viewMode: Flow<ConnectionViewMode>
    suspend fun setViewMode(mode: ConnectionViewMode)
}
