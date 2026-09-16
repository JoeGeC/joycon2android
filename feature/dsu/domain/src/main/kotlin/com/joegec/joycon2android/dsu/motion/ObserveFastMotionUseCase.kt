package com.joegec.joycon2android.dsu.motion

import kotlinx.coroutines.flow.Flow

class ObserveFastMotionUseCase(private val preferences: FastMotionPreferences) {
    operator fun invoke(): Flow<Boolean> = preferences.fastMotion
}
