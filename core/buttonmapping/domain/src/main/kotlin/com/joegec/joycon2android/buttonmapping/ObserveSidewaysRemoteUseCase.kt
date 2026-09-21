package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Whether a console plays as a sideways Wii Remote: the user's choice, else its layout's. */
class ObserveSidewaysRemoteUseCase(
    private val repository: SidewaysRemoteRepository,
    private val observeMappingPreset: ObserveMappingPresetUseCase,
) {
    operator fun invoke(console: Console): Flow<Boolean> =
        combine(repository.observe(console), observeMappingPreset(console)) { chosen, preset ->
            chosen ?: preset.sidewaysRemote
        }
}
