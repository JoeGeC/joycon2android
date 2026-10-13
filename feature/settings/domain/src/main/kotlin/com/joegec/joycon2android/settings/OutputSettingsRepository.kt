package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

interface OutputSettingsRepository {
    val settings: Flow<OutputSettings>
    suspend fun setFasterUpdates(enabled: Boolean)
    suspend fun setBlockDeviceMotion(enabled: Boolean)
}
