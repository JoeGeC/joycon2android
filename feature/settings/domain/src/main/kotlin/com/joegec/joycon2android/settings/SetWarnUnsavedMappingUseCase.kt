package com.joegec.joycon2android.settings

class SetWarnUnsavedMappingUseCase(private val repository: UnsavedMappingWarningRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setWarnUnsavedMapping(enabled)
}
