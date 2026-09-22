package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console

internal const val MARIO_KART = "Mario Kart"

/** Every layout the app ships, and which one a console falls back to. */
object MappingPresets {

    private val all = listOf(
        GameCubeMapping,
        WiiMapping,
        JoyconWiiMapping,
        MarioKartWheelMapping,
        MarioKartNunchukMapping,
        SwitchProMapping,
    )

    fun forConsole(console: Console): List<MappingPreset> = all.filter { it.console == console }

    fun default(console: Console): MappingPreset = forConsole(console).first()

    /** Falls back to the default for an id from a build that offered a preset this one doesn't. */
    fun byId(console: Console, id: String?): MappingPreset =
        forConsole(console).firstOrNull { it.id == id } ?: default(console)
}
