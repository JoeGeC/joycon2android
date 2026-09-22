package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * What a player's body is bound to: whatever has been set on it, over the console's default layout
 * so that every target is answered even when nothing has ever set that one.
 */
class ObserveControllerMappingUseCase(private val repository: ControllerMappingRepository) {
    operator fun invoke(console: Console, body: PlayerBody): Flow<Map<String, String>> =
        repository.observe(console, body).map { stored ->
            MappingPresets.default(console).entries(body.side) +
                stored.withLegacyButtonNamesRenamed().withLegacyStickRoutesExpanded()
        }
}
