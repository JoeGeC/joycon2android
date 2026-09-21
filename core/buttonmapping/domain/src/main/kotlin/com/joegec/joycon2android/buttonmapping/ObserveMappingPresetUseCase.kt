package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPreset
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The layout a console is set to, falling back to its default. */
class ObserveMappingPresetUseCase(private val repository: MappingPresetRepository) {
    operator fun invoke(console: Console): Flow<MappingPreset> =
        repository.observe(console).map { MappingPresets.byId(console, it) }
}
