package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.LayoutFamily
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.StickSource.LEFT_STICK
import com.joegec.joycon2android.buttonmapping.StickSource.RIGHT_STICK
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconButton.A
import com.joegec.joycon2android.model.JoyconButton.B
import com.joegec.joycon2android.model.JoyconButton.Capture
import com.joegec.joycon2android.model.JoyconButton.Down
import com.joegec.joycon2android.model.JoyconButton.Home
import com.joegec.joycon2android.model.JoyconButton.Left
import com.joegec.joycon2android.model.JoyconButton.L
import com.joegec.joycon2android.model.JoyconButton.Minus
import com.joegec.joycon2android.model.JoyconButton.Plus
import com.joegec.joycon2android.model.JoyconButton.R
import com.joegec.joycon2android.model.JoyconButton.Right
import com.joegec.joycon2android.model.JoyconButton.SlLeft
import com.joegec.joycon2android.model.JoyconButton.SlRight
import com.joegec.joycon2android.model.JoyconButton.SrLeft
import com.joegec.joycon2android.model.JoyconButton.SrRight
import com.joegec.joycon2android.model.JoyconButton.X
import com.joegec.joycon2android.model.JoyconButton.Y
import com.joegec.joycon2android.model.JoyconButton.ZL
import com.joegec.joycon2android.model.JoyconButton.ZR

/**
 * Steers by stick. A pair puts B and Z on the upper shoulders, 1 and 2 on the lower; a lone Joy-Con
 * plays both halves, its stick as the Nunchuk's and its rails as Z and B.
 */
object MarioKartNunchukMapping : MappingPreset {
    override val id = "MARIO_KART_NUNCHUK"
    override val console = Console.WIIMOTE_NUNCHUK
    override val family = LayoutFamily.MARIO_KART
    override val sidewaysRemote = true

    override fun entries(side: JoyconSide) = when (side) {
        JoyconSide.DUAL -> WiiMapping.entries(side) + pairButtons()
        else -> loneButtons(side) + nunchukStick(side).stickEntries()
    }

    private fun pairButtons() = mapOf(
        WiimoteButton.One to listOf(ZL),
        WiimoteButton.Two to listOf(ZR),
        WiimoteButton.B to listOf(R, B),
        WiimoteButton.Shake to listOf(R),
        WiimoteButton.Minus to listOf(Minus),
        WiimoteButton.NunchukC to listOf(X),
        WiimoteButton.NunchukZ to listOf(L),
    ).sources()

    private fun loneButtons(side: JoyconSide) = (loneFaces(side) + UNBOUND).sources()

    private fun loneFaces(side: JoyconSide) = when (side) {
        JoyconSide.LEFT -> mapOf(
            WiimoteButton.A to listOf(Down),
            WiimoteButton.B to listOf(Left, SrLeft),
            WiimoteButton.NunchukC to listOf(Right),
            WiimoteButton.NunchukZ to listOf(SlLeft),
            WiimoteButton.Plus to listOf(Minus),
            WiimoteButton.Home to listOf(Capture),
            WiimoteButton.Shake to listOf(SrLeft),
        )
        else -> mapOf(
            WiimoteButton.A to listOf(X),
            WiimoteButton.B to listOf(A, SrRight),
            WiimoteButton.NunchukC to listOf(Y),
            WiimoteButton.NunchukZ to listOf(SlRight),
            WiimoteButton.Plus to listOf(Plus),
            WiimoteButton.Home to listOf(Home),
            WiimoteButton.Shake to listOf(SrRight),
        )
    }

    // Listed rather than omitted, or they keep the console default's bindings ([MappingLayouts.entriesOf]).
    private val UNBOUND = listOf(
        WiimoteButton.DPadUp,
        WiimoteButton.DPadDown,
        WiimoteButton.DPadLeft,
        WiimoteButton.DPadRight,
        WiimoteButton.One,
        WiimoteButton.Two,
        WiimoteButton.Minus,
    ).associateWith { emptyList<JoyconButton>() }

    private fun nunchukStick(side: JoyconSide): Map<WiimoteStick, StickSource> =
        mapOf(WiimoteStick.NunchukStick to if (side == JoyconSide.LEFT) LEFT_STICK else RIGHT_STICK)

    private fun Map<WiimoteButton, List<JoyconButton>>.sources() =
        mapValues { (_, buttons) -> buttons.map(MappingSource::Button) }.sourceEntries()
}
