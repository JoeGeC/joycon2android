package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.target.PadButton

/** Android games place A at the bottom, as Xbox pads do; Nintendo prints it on the right. */
object AndroidXboxMapping : MappingPreset {
    override val id = "XBOX_POSITIONS"
    override val console = Console.ANDROID

    override fun entries(side: JoyconSide) = AndroidMapping.entries(side) + swapped(side)

    private fun swapped(side: JoyconSide): Map<String, String> {
        val buttons = AndroidMapping.buttons(side)
        return SWAPS.mapNotNull { (target, from) -> buttons[from]?.let { target to it } }.toMap().buttonEntries()
    }

    private val SWAPS = mapOf(
        PadButton.A to PadButton.B,
        PadButton.B to PadButton.A,
        PadButton.X to PadButton.Y,
        PadButton.Y to PadButton.X,
    )
}
