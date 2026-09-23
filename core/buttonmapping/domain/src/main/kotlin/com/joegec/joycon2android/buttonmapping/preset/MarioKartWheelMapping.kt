package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.LayoutFamily
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconButton.A
import com.joegec.joycon2android.model.JoyconButton.B
import com.joegec.joycon2android.model.JoyconButton.Capture
import com.joegec.joycon2android.model.JoyconButton.Down
import com.joegec.joycon2android.model.JoyconButton.Home
import com.joegec.joycon2android.model.JoyconButton.Left
import com.joegec.joycon2android.model.JoyconButton.Minus
import com.joegec.joycon2android.model.JoyconButton.Plus
import com.joegec.joycon2android.model.JoyconButton.Right
import com.joegec.joycon2android.model.JoyconButton.SlLeft
import com.joegec.joycon2android.model.JoyconButton.SlRight
import com.joegec.joycon2android.model.JoyconButton.SrLeft
import com.joegec.joycon2android.model.JoyconButton.SrRight
import com.joegec.joycon2android.model.JoyconButton.Up
import com.joegec.joycon2android.model.JoyconButton.X
import com.joegec.joycon2android.model.JoyconButton.Y

/** Mario Kart 8's sideways layout, so a thumb does the same job in both games; SL also throws an item. */
object MarioKartWheelMapping : MappingPreset {
    override val id = "MARIO_KART_WHEEL"
    override val console = Console.WIIMOTE_NUNCHUK
    override val family = LayoutFamily.MARIO_KART
    override val sides = setOf(JoyconSide.LEFT, JoyconSide.RIGHT)
    override val sidewaysRemote = true

    override fun entries(side: JoyconSide) =
        buttons(side).buttonEntries() + dPadSticks(side).sourceEntries()

    private fun buttons(side: JoyconSide): Map<WiimoteButton, JoyconButton> = when (side) {
        JoyconSide.LEFT -> mapOf(
            WiimoteButton.A to Right,
            WiimoteButton.B to SrLeft,
            WiimoteButton.One to Left,
            WiimoteButton.Two to Down,
            WiimoteButton.Home to Capture,
            WiimoteButton.Plus to Minus,
            WiimoteButton.Minus to Up,
            WiimoteButton.Shake to SrLeft,
        )
        else -> mapOf(
            WiimoteButton.A to Y,
            WiimoteButton.B to SrRight,
            WiimoteButton.One to A,
            WiimoteButton.Two to X,
            WiimoteButton.Home to Home,
            WiimoteButton.Plus to Plus,
            WiimoteButton.Minus to B,
            WiimoteButton.Shake to SrRight,
        )
    }

    private fun dPadSticks(side: JoyconSide): Map<WiimoteButton, List<MappingSource>> {
        val fromStick = WiiMapping.dPadSticks(side)
        val item = MappingSource.Button(if (side == JoyconSide.LEFT) SlLeft else SlRight)
        return fromStick + (WiimoteButton.DPadUp to fromStick.getValue(WiimoteButton.DPadUp) + item)
    }
}
