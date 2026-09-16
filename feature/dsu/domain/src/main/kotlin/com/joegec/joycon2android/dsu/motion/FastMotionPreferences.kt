package com.joegec.joycon2android.dsu.motion

import kotlinx.coroutines.flow.Flow

interface FastMotionPreferences {
    val fastMotion: Flow<Boolean>
    suspend fun setFastMotion(enabled: Boolean)
}
