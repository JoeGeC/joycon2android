package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The mapping actually in effect for a console/body: stored overrides layered on the defaults. */
class ObserveControllerMappingUseCase(private val repository: ControllerMappingRepository) {
    operator fun invoke(console: Console, side: JoyconSide): Flow<Map<String, String>> =
        repository.observe(console, side).map { stored -> defaultsFor(console, side) + stored }
}

private fun defaultsFor(console: Console, side: JoyconSide): Map<String, String> = when (console) {
    Console.GAMECUBE -> DefaultControllerMappings.gameCubeButtons(side).toStringMap() +
        DefaultControllerMappings.gameCubeSticks(side).toStringMap()
    Console.WIIMOTE_NUNCHUK -> DefaultControllerMappings.wiimoteButtons(side).toStringMap() +
        DefaultControllerMappings.wiimoteSticks(side).toStringMap()
    Console.SWITCH_PRO -> DefaultControllerMappings.switchProButtons(side).toStringMap() +
        DefaultControllerMappings.switchProSticks(side).toStringMap()
}

private fun <K : Enum<K>, V : Enum<V>> Map<K, V>.toStringMap(): Map<String, String> =
    entries.associate { it.key.name to it.value.name }
