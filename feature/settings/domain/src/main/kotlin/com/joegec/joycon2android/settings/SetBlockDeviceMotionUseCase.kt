package com.joegec.joycon2android.settings

class SetBlockDeviceMotionUseCase(private val repository: OutputSettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setBlockDeviceMotion(enabled)
}
