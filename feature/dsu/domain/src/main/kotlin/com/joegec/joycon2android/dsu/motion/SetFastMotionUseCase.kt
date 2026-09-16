package com.joegec.joycon2android.dsu.motion

class SetFastMotionUseCase(private val preferences: FastMotionPreferences) {
    suspend operator fun invoke(enabled: Boolean) = preferences.setFastMotion(enabled)
}
