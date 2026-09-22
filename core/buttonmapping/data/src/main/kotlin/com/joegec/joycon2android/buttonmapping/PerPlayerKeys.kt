package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerNumber

private const val SEPARATOR = "|"

internal fun bodyKeyPrefix(console: Console, body: PlayerBody) =
    listOf(console.name, body.player.name, body.side.name).joinToString(SEPARATOR)

internal fun segmentsOf(name: String) = name.split(SEPARATOR)

internal fun consoleNamed(name: String) = Console.entries.firstOrNull { it.name == name }

internal fun sideNamed(name: String) = JoyconSide.entries.firstOrNull { it.name == name }

/** Every body a console-wide setting now belongs to, in the new key's shape. */
internal fun everyBodyKey(console: Console, target: String? = null): List<String> =
    PlayerNumber.entries.flatMap { player ->
        JoyconSide.entries.map { side ->
            listOfNotNull(console.name, player.name, side.name, target).joinToString(SEPARATOR)
        }
    }

/** Every player's copy of a setting that already names the body it belongs to. */
internal fun everyPlayerKey(console: Console, side: JoyconSide, target: String): List<String> =
    PlayerNumber.entries.map { player ->
        listOf(console.name, player.name, side.name, target).joinToString(SEPARATOR)
    }
