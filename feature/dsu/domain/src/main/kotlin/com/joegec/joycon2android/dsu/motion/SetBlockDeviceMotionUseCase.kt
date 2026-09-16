package com.joegec.joycon2android.dsu.motion

class SetBlockDeviceMotionUseCase(private val repository: DsuMotionSettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setBlockDeviceMotion(enabled)
}
