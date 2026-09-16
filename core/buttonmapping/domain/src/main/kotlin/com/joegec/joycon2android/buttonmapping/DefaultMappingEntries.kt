package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton

/** The shipped defaults for a console/body, in the repository's opaque string form. */
fun defaultMappingEntries(console: Console, side: JoyconSide): Map<String, String> = when (console) {
    Console.GAMECUBE -> DefaultControllerMappings.gameCubeButtons(side).buttonEntries() +
        DefaultControllerMappings.gameCubeSticks(side).stickEntries()
    Console.WIIMOTE_NUNCHUK -> DefaultControllerMappings.wiimoteButtons(side).buttonEntries() +
        DefaultControllerMappings.wiimoteDPadSticks(side).sourceEntries() +
        DefaultControllerMappings.wiimoteSticks(side).stickEntries()
    Console.SWITCH_PRO -> DefaultControllerMappings.switchProButtons(side).buttonEntries() +
        DefaultControllerMappings.switchProSticks(side).stickEntries()
}

private fun <K : Enum<K>> Map<K, JoyconButton>.buttonEntries(): Map<String, String> =
    entries.associate { (target, button) -> target.name to button.name }

private fun <K : Enum<K>> Map<K, MappingSource>.sourceEntries(): Map<String, String> =
    entries.associate { (target, source) -> target.name to source.id }

private fun <K : Enum<K>> Map<K, StickSource>.stickEntries(): Map<String, String> =
    entries.flatMap { (target, stick) ->
        MappingSource.directionsOf(stick).map { target.directionKey(it.direction) to it.id }
    }.toMap()
