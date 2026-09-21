package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** The mapping actually in effect for a console/body: stored overrides layered on its preset. */
class ObserveControllerMappingUseCase(
    private val repository: ControllerMappingRepository,
    private val observeMappingPreset: ObserveMappingPresetUseCase,
) {
    operator fun invoke(console: Console, side: JoyconSide): Flow<Map<String, String>> =
        combine(observeMappingPreset(console), repository.observe(console, side)) { preset, stored ->
            preset.entries(side) + stored.withLegacyButtonNamesRenamed().withLegacyStickRoutesExpanded()
        }
}
