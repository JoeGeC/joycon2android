package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.CLASSIC_CONTROLLER_KEY
import com.joegec.joycon2android.buttonmapping.CLASSIC_CONTROLLER_ON
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.StickSource.LEFT_STICK
import com.joegec.joycon2android.buttonmapping.StickSource.RIGHT_STICK
import com.joegec.joycon2android.buttonmapping.target.ClassicButton
import com.joegec.joycon2android.buttonmapping.target.ClassicStick
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
import com.joegec.joycon2android.model.JoyconButton.ZL
import com.joegec.joycon2android.model.JoyconButton.ZR

/** Plugs in a Classic Controller in place of the Nunchuk, its buttons where a Pro Controller has them. */
object ClassicControllerMapping : MappingPreset {
    override val id = "CLASSIC"
    override val console = Console.WIIMOTE_NUNCHUK

    override fun entries(side: JoyconSide) =
        buttons(side).buttonEntries() + sticks(side).stickEntries() +
            (CLASSIC_CONTROLLER_KEY to CLASSIC_CONTROLLER_ON)

    private val PAIR_BUTTONS = mapOf(
        ClassicButton.ClassicA to A,
        ClassicButton.ClassicB to B,
        ClassicButton.ClassicX to X,
        ClassicButton.ClassicY to Y,
        ClassicButton.ClassicL to L,
        ClassicButton.ClassicR to R,
        ClassicButton.ClassicZL to ZL,
        ClassicButton.ClassicZR to ZR,
        ClassicButton.ClassicPlus to Plus,
        ClassicButton.ClassicMinus to Minus,
        ClassicButton.ClassicHome to Home,
        ClassicButton.ClassicDPadUp to Up,
        ClassicButton.ClassicDPadDown to Down,
        ClassicButton.ClassicDPadLeft to Left,
        ClassicButton.ClassicDPadRight to Right,
    )

    // Sideways, as SwitchProSidewaysLayout holds a lone Joy-Con.
    private fun buttons(side: JoyconSide): Map<ClassicButton, JoyconButton> = when (side) {
        JoyconSide.DUAL -> PAIR_BUTTONS
        // No −, so Capture takes it.
        JoyconSide.GAMECUBE -> PAIR_BUTTONS + (ClassicButton.ClassicMinus to Capture)
        JoyconSide.LEFT -> mapOf(
            ClassicButton.ClassicA to Down,
            ClassicButton.ClassicB to Left,
            ClassicButton.ClassicX to Right,
            ClassicButton.ClassicY to Up,
            ClassicButton.ClassicL to SlLeft,
            ClassicButton.ClassicR to SrLeft,
            ClassicButton.ClassicPlus to Minus,
            ClassicButton.ClassicHome to Capture,
        )
        JoyconSide.RIGHT -> mapOf(
            ClassicButton.ClassicA to X,
            ClassicButton.ClassicB to A,
            ClassicButton.ClassicX to Y,
            ClassicButton.ClassicY to B,
            ClassicButton.ClassicL to SlRight,
            ClassicButton.ClassicR to SrRight,
            ClassicButton.ClassicPlus to Plus,
            ClassicButton.ClassicHome to Home,
        )
    }

    private fun sticks(side: JoyconSide): Map<ClassicStick, StickSource> =
        loneStick(side)?.let { mapOf(ClassicStick.ClassicLeftStick to it) }
            ?: mapOf(ClassicStick.ClassicLeftStick to LEFT_STICK, ClassicStick.ClassicRightStick to RIGHT_STICK)

    private fun loneStick(side: JoyconSide): StickSource? = when (side) {
        JoyconSide.DUAL, JoyconSide.GAMECUBE -> null
        JoyconSide.LEFT -> LEFT_STICK
        JoyconSide.RIGHT -> RIGHT_STICK
    }
}
