package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.Flow

interface ControllerTypePreferences {
    val overrides: Flow<Map<String, Side>>
    suspend fun set(address: String, side: Side?)
}
