package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide

/** The pad passes every button straight to the game, so only device keys are left to bind. */
object AndroidMapping : MappingPreset {
    override val id = "STANDARD"
    override val console = Console.ANDROID

    override fun entries(side: JoyconSide): Map<String, String> = emptyMap()
}
