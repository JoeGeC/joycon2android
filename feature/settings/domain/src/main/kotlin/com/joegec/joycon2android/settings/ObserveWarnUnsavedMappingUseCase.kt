package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

class ObserveWarnUnsavedMappingUseCase(private val repository: UnsavedMappingWarningRepository) {
    operator fun invoke(): Flow<Boolean> = repository.warnUnsavedMapping
}
