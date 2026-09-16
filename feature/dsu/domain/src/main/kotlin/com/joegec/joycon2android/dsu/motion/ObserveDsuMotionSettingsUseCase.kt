package com.joegec.joycon2android.dsu.motion

import kotlinx.coroutines.flow.Flow

class ObserveDsuMotionSettingsUseCase(private val repository: DsuMotionSettingsRepository) {
    operator fun invoke(): Flow<DsuMotionSettings> = repository.settings
}
