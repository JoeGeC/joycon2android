package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

/** One-shot read of the mapping in effect, for the emulator-config generators at "Set up" time. */
class GetEffectiveControllerMappingUseCase(
    private val observeControllerMapping: ObserveControllerMappingUseCase,
) {
    suspend operator fun invoke(console: Console, side: JoyconSide): Map<String, String> =
        observeControllerMapping(console, side).first()
}
