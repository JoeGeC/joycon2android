package com.joegec.joycon2android.dsu.motion

import kotlinx.coroutines.flow.Flow

interface DsuMotionSettingsRepository {
    val settings: Flow<DsuMotionSettings>
    suspend fun setFastMotion(enabled: Boolean)
    suspend fun setBlockDeviceMotion(enabled: Boolean)
}
