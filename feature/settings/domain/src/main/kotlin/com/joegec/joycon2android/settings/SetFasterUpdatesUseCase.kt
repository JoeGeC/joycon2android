package com.joegec.joycon2android.settings

class SetFasterUpdatesUseCase(private val repository: OutputSettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setFasterUpdates(enabled)
}
