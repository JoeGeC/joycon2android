package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
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
import com.joegec.joycon2android.model.JoyconButton.L
import com.joegec.joycon2android.model.JoyconButton.Left
import com.joegec.joycon2android.model.JoyconButton.Minus
import com.joegec.joycon2android.model.JoyconButton.Plus
import com.joegec.joycon2android.model.JoyconButton.Right
import com.joegec.joycon2android.model.JoyconButton.Up
import com.joegec.joycon2android.model.JoyconButton.X
import com.joegec.joycon2android.model.JoyconButton.Y
import com.joegec.joycon2android.model.JoyconButton.ZL
import com.joegec.joycon2android.model.JoyconButton.ZR

/** The Wii Remote's own layout: the trigger under the finger is B, and 1 and 2 sit under the thumb. */
object WiiMapping : MappingPreset {
    override val id = "WII"
    override val console = Console.WIIMOTE_NUNCHUK

    override fun entries(side: JoyconSide) =
        buttons(side).buttonEntries() + dPadSticks(side).sourceEntries() + nunchukStick(side).stickEntries()

    internal fun buttons(side: JoyconSide): Map<WiimoteButton, JoyconButton> = when (side) {
        JoyconSide.DUAL -> mapOf(
            WiimoteButton.A to A,
            WiimoteButton.B to ZR,
            WiimoteButton.One to Y,
            WiimoteButton.Two to B,
            WiimoteButton.Home to Home,
            WiimoteButton.Plus to Plus,
            WiimoteButton.Minus to X,
            WiimoteButton.DPadUp to Up,
            WiimoteButton.DPadDown to Down,
            WiimoteButton.DPadLeft to Left,
            WiimoteButton.DPadRight to Right,
            WiimoteButton.NunchukC to L,
            WiimoteButton.NunchukZ to ZL,
        )
        JoyconSide.LEFT -> mapOf(
            WiimoteButton.A to Down,
            WiimoteButton.B to ZL,
            WiimoteButton.One to Up,
            WiimoteButton.Two to Left,
            WiimoteButton.Home to Capture,
            WiimoteButton.Plus to Right,
            WiimoteButton.Minus to Minus,
        )
        JoyconSide.RIGHT -> mapOf(
            WiimoteButton.A to A,
            WiimoteButton.B to ZR,
            WiimoteButton.One to Y,
            WiimoteButton.Two to B,
            WiimoteButton.Home to Home,
            WiimoteButton.Plus to Plus,
            WiimoteButton.Minus to X,
        )
    }

    // A sideways Joy-Con has no d-pad left once its cluster becomes the face buttons, so its stick
    // steers the Wii Remote's d-pad instead.
    internal fun dPadSticks(side: JoyconSide): Map<WiimoteButton, List<MappingSource>> {
        val stick = when (side) {
            JoyconSide.DUAL -> return emptyMap()
            JoyconSide.LEFT -> LEFT_STICK
            JoyconSide.RIGHT -> RIGHT_STICK
        }
        return mapOf(
            WiimoteButton.DPadUp to tilt(stick, StickDirection.UP),
            WiimoteButton.DPadDown to tilt(stick, StickDirection.DOWN),
            WiimoteButton.DPadLeft to tilt(stick, StickDirection.LEFT),
            WiimoteButton.DPadRight to tilt(stick, StickDirection.RIGHT),
        )
    }

    internal fun nunchukStick(side: JoyconSide): Map<WiimoteStick, StickSource> = when (side) {
        JoyconSide.DUAL -> mapOf(WiimoteStick.NunchukStick to LEFT_STICK)
        else -> emptyMap()
    }

    private fun tilt(stick: StickSource, direction: StickDirection) =
        listOf<MappingSource>(MappingSource.Stick(stick, direction))
}
