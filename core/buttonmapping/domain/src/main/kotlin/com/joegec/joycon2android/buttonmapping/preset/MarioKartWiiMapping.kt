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
import com.joegec.joycon2android.model.JoyconButton.L
import com.joegec.joycon2android.model.JoyconButton.Left
import com.joegec.joycon2android.model.JoyconButton.Minus
import com.joegec.joycon2android.model.JoyconButton.Plus
import com.joegec.joycon2android.model.JoyconButton.R
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
 * A pair keeps the [WiiMapping] layout's shape — held two-handed there is no sideways grip to match
 * — but moves the jobs an index finger does onto the shoulders that finger already rests on.
 *
 * It is also the layout that plays as a sideways Wii Remote, which is what the wheel steers by.
 */
object MarioKartWiiMapping : MappingPreset {
    override val id = "MARIO_KART"
    override val displayName = "Mario Kart"
    override val console = Console.WIIMOTE_NUNCHUK
    override val sidewaysRemote = true

    override fun entries(side: JoyconSide) = when (side) {
        JoyconSide.DUAL -> WiiMapping.entries(side) + pairButtons()
        else -> buttons(side).buttonEntries() + dPadSticks(side).sourceEntries()
    }

    // Held as a remote and a nunchuk, each index finger rests on that hand's shoulder, which is
    // where the controller it stands in for keeps its trigger: the remote's B on the right, the
    // Nunchuk's Z on the left. Hopping also keeps the Joy-Con's own B, so either the thumb or the
    // index finger can do it. The trick rides the same shoulder as the hop, as SR does on a lone
    // Joy-Con, so the finger that jumps is the finger that tricks.
    private fun pairButtons(): Map<String, String> = mapOf(
        WiimoteButton.B to listOf(R, B),
        WiimoteButton.Shake to listOf(R),
        WiimoteButton.Minus to listOf(Minus),
        WiimoteButton.NunchukC to listOf(X),
        WiimoteButton.NunchukZ to listOf(L),
    ).mapValues { (_, buttons) -> buttons.map(MappingSource::Button) }.sourceEntries()

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
        val throwItem = fromStick.getValue(WiimoteButton.DPadUp) + item
        return fromStick + (WiimoteButton.DPadUp to throwItem)
    }
}
