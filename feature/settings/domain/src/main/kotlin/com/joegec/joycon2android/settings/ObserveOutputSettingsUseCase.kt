package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

class ObserveOutputSettingsUseCase(private val repository: OutputSettingsRepository) {
    operator fun invoke(): Flow<OutputSettings> = repository.settings
}
