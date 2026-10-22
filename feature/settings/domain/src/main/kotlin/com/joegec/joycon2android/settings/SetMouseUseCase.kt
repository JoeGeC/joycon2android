package com.joegec.joycon2android.settings

class SetMouseUseCase(private val repository: OutputSettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setMouse(enabled)
}
