package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.ClassicControllerMapping
import kotlinx.coroutines.flow.first

/** Overrides the layout's answer until a layout is next applied. A first Classic Controller gets its layout's bindings. */
class SetWiiControllerUseCase(
    private val repository: WiiControllerRepository,
    private val observeControllerMapping: ObserveControllerMappingUseCase,
    private val mappingRepository: ControllerMappingRepository,
) {
    suspend operator fun invoke(console: Console, body: PlayerBody, controller: WiiController) {
        if (controller == WiiController.CLASSIC) bindClassicControllerIfUnbound(console, body)
        repository.set(console, body, controller)
    }

    private suspend fun bindClassicControllerIfUnbound(console: Console, body: PlayerBody) {
        val current = observeControllerMapping(console, body).first()
        val classic = ClassicControllerMapping.controllerEntries(body.side)
        if (classic.keys.any { it in current }) return
        mappingRepository.replace(console, body, current + classic)
    }
}
