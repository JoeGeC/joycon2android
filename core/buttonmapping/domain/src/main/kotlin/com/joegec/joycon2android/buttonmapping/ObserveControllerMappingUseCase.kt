package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The mapping actually in effect for a console/body: stored overrides layered on the defaults. */
class ObserveControllerMappingUseCase(private val repository: ControllerMappingRepository) {
    operator fun invoke(console: Console, side: JoyconSide): Flow<Map<String, String>> =
        repository.observe(console, side).map { stored ->
            defaultMappingEntries(console, side) + stored.withLegacyStickRoutesExpanded()
        }
}
