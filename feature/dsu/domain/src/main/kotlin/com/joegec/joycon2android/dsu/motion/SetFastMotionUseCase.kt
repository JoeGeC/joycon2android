package com.joegec.joycon2android.dsu.motion

class SetFastMotionUseCase(private val repository: DsuMotionSettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setFastMotion(enabled)
}
