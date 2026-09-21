package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton

/** The Wii layout with the remote's B and 2 swapped, so the Joy-Con's own B is the remote's B. */
object JoyconWiiMapping : MappingPreset {
    override val id = "JOYCON"
    override val displayName = "Joy-Con"
    override val console = Console.WIIMOTE_NUNCHUK

    override fun entries(side: JoyconSide) =
        WiiMapping.entries(side).swappingSources(WiimoteButton.B, WiimoteButton.Two)
}
