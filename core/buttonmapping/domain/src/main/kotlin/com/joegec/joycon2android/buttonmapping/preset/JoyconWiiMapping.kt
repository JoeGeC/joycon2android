package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconButton.B
import com.joegec.joycon2android.model.JoyconButton.Down
import com.joegec.joycon2android.model.JoyconButton.L
import com.joegec.joycon2android.model.JoyconButton.LS
import com.joegec.joycon2android.model.JoyconButton.Minus
import com.joegec.joycon2android.model.JoyconButton.R
import com.joegec.joycon2android.model.JoyconButton.RS
import com.joegec.joycon2android.model.JoyconButton.Right
import com.joegec.joycon2android.model.JoyconButton.Up
import com.joegec.joycon2android.model.JoyconButton.ZL
import com.joegec.joycon2android.model.JoyconButton.ZR

/** The Wii layout with B on the Joy-Con's B, 1 and 2 on the shoulders so the thumb stays on the stick, and Recenter on its click. */
object JoyconWiiMapping : MappingPreset {
    override val id = "JOYCON"
    override val console = Console.WIIMOTE_NUNCHUK

    override fun entries(side: JoyconSide) = WiiMapping.entries(side) + buttons(side).buttonEntries()

    private fun buttons(side: JoyconSide): Map<WiimoteButton, JoyconButton> = when (side) {
        JoyconSide.DUAL -> mapOf(
            WiimoteButton.B to B,
            WiimoteButton.One to R,
            WiimoteButton.Two to ZR,
            WiimoteButton.Minus to Minus,
            WiimoteButton.Recenter to RS,
        )
        JoyconSide.RIGHT -> mapOf(
            WiimoteButton.B to B,
            WiimoteButton.One to R,
            WiimoteButton.Two to ZR,
            WiimoteButton.Recenter to RS,
        )
        JoyconSide.LEFT -> mapOf(
            WiimoteButton.A to Right,
            WiimoteButton.B to Down,
            WiimoteButton.One to L,
            WiimoteButton.Two to ZL,
            WiimoteButton.Plus to Up,
            WiimoteButton.Recenter to LS,
        )
    }
}
