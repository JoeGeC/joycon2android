package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide

/** The emulator is mapped in its own settings, so only device keys are left to bind. */
object OtherEmulatorMapping : MappingPreset {
    override val id = "STANDARD"
    override val console = Console.EMULATOR

    override fun entries(side: JoyconSide): Map<String, String> = emptyMap()
}
