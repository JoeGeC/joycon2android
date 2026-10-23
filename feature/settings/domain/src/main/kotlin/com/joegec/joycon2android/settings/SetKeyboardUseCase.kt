package com.joegec.joycon2android.settings

class SetKeyboardUseCase(private val repository: OutputSettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setKeyboard(enabled)
}
