package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel
import kotlinx.coroutines.flow.Flow

interface ControllerTypePreferences {
    val overrides: Flow<Map<String, ControllerModel>>
    suspend fun set(address: String, model: ControllerModel?)
}
