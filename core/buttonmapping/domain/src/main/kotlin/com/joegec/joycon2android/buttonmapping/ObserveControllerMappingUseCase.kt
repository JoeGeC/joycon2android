package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Layered over the console's default layout, so every target is answered. */
class ObserveControllerMappingUseCase(private val repository: ControllerMappingRepository) {
    operator fun invoke(console: Console, body: PlayerBody): Flow<Map<String, String>> =
        repository.observe(console, body).map { stored ->
            MappingPresets.default(console).entries(body.side) +
                stored.withLegacyButtonNamesRenamed().withLegacyStickRoutesExpanded()
        }
}
