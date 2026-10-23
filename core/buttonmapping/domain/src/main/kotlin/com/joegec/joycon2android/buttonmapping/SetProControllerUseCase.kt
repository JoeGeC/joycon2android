package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import kotlinx.coroutines.flow.first

/** The two grips bind different targets, so switching resets to that grip's layout, keeping the device keys. */
class SetProControllerUseCase(
    private val observeControllerMapping: ObserveControllerMappingUseCase,
    private val repository: ControllerMappingRepository,
) {
    suspend operator fun invoke(console: Console, body: PlayerBody, enabled: Boolean) {
        val layout = MappingPresets.forConsole(console)
            .firstOrNull { body.side in it.sides && it.entries(body.side).proController == enabled }
            ?: return
        val deviceKeys = observeControllerMapping(console, body).first().filterKeys { it in DEVICE_KEY_TARGETS }
        repository.replace(console, body, MappingLayouts.entriesOf(console, body.side, layout) + deviceKeys)
    }
}
