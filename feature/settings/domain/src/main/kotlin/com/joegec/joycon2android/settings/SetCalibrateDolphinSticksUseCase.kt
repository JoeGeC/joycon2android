package com.joegec.joycon2android.settings

class SetCalibrateDolphinSticksUseCase(private val repository: OutputSettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setCalibrateDolphinSticks(enabled)
}
