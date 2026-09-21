package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
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

/**
 * A sideways Joy-Con laid out the way Mario Kart 8 uses one, so the same thumb does the same job in
 * both games: accelerate on 2, brake on 1, hop on SR. Mario Kart Wii throws an item with the d-pad,
 * which a sideways body already steers from its stick, so SL fires it too — the shoulder that
 * throws in Mario Kart 8.
 *
 * A pair keeps the [WiiMapping] layout: held two-handed there is no sideways grip to match.
 *
 * It is also the layout that plays as a sideways Wii Remote, which is what the wheel steers by.
 */
object MarioKartWiiMapping : MappingPreset {
    override val id = "MARIO_KART"
    override val displayName = "Mario Kart"
    override val console = Console.WIIMOTE_NUNCHUK
    override val sidewaysRemote = true

    override fun entries(side: JoyconSide) = when (side) {
        JoyconSide.DUAL -> WiiMapping.entries(side)
        else -> buttons(side).buttonEntries() + dPadSticks(side).sourceEntries()
    }

    private fun buttons(side: JoyconSide): Map<WiimoteButton, JoyconButton> = when (side) {
        JoyconSide.LEFT -> mapOf(
            WiimoteButton.A to Right,
            WiimoteButton.B to SrLeft,
            WiimoteButton.One to Left,
            WiimoteButton.Two to Down,
            WiimoteButton.Home to Capture,
            WiimoteButton.Plus to Minus,
            WiimoteButton.Minus to Up,
        )
        else -> mapOf(
            WiimoteButton.A to Y,
            WiimoteButton.B to SrRight,
            WiimoteButton.One to A,
            WiimoteButton.Two to X,
            WiimoteButton.Home to Home,
            WiimoteButton.Plus to Plus,
            WiimoteButton.Minus to B,
        )
    }

    private fun dPadSticks(side: JoyconSide): Map<WiimoteButton, List<MappingSource>> {
        val fromStick = WiiMapping.dPadSticks(side)
        val item = MappingSource.Button(if (side == JoyconSide.LEFT) SlLeft else SlRight)
        val throwItem = fromStick.getValue(WiimoteButton.DPadUp) + item
        return fromStick + (WiimoteButton.DPadUp to throwItem)
    }
}
