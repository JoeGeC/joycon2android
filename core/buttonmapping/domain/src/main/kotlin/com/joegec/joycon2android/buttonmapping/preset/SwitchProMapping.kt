package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.StickSource.LEFT_STICK
import com.joegec.joycon2android.buttonmapping.StickSource.RIGHT_STICK
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconButton.A
import com.joegec.joycon2android.model.JoyconButton.B
import com.joegec.joycon2android.model.JoyconButton.Capture
import com.joegec.joycon2android.model.JoyconButton.Down
import com.joegec.joycon2android.model.JoyconButton.Home
import com.joegec.joycon2android.model.JoyconButton.L
import com.joegec.joycon2android.model.JoyconButton.LS
import com.joegec.joycon2android.model.JoyconButton.Left
import com.joegec.joycon2android.model.JoyconButton.Minus
import com.joegec.joycon2android.model.JoyconButton.Plus
import com.joegec.joycon2android.model.JoyconButton.R
import com.joegec.joycon2android.model.JoyconButton.RS
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

object SwitchProMapping : MappingPreset {
    override val id = "STANDARD"
    override val console = Console.SWITCH_PRO

    override fun entries(side: JoyconSide) = buttons(side).buttonEntries() + sticks(side).stickEntries()

    private fun buttons(side: JoyconSide): Map<SwitchProButton, JoyconButton> = when (side) {
        JoyconSide.DUAL -> mapOf(
            SwitchProButton.A to A,
            SwitchProButton.B to B,
            SwitchProButton.X to X,
            SwitchProButton.Y to Y,
            SwitchProButton.L to L,
            SwitchProButton.R to R,
            SwitchProButton.ZL to ZL,
            SwitchProButton.ZR to ZR,
            SwitchProButton.Plus to Plus,
            SwitchProButton.Minus to Minus,
            SwitchProButton.Home to Home,
            SwitchProButton.Capture to Capture,
            SwitchProButton.LStickClick to LS,
            SwitchProButton.RStickClick to RS,
            SwitchProButton.DPadUp to Up,
            SwitchProButton.DPadDown to Down,
            SwitchProButton.DPadLeft to Left,
            SwitchProButton.DPadRight to Right,
        )
        // Rails as shoulders, ZL/ZR unbound: docs/virtual-gamepad.md#sidewaysmapper
        JoyconSide.LEFT -> mapOf(
            SwitchProButton.A to Down,
            SwitchProButton.B to Left,
            SwitchProButton.X to Right,
            SwitchProButton.Y to Up,
            SwitchProButton.L to SlLeft,
            SwitchProButton.R to SrLeft,
            SwitchProButton.Minus to Minus,
            SwitchProButton.LStickClick to LS,
            SwitchProButton.Capture to Capture,
        )
        JoyconSide.RIGHT -> mapOf(
            SwitchProButton.A to X,
            SwitchProButton.B to A,
            SwitchProButton.X to Y,
            SwitchProButton.Y to B,
            SwitchProButton.L to SlRight,
            SwitchProButton.R to SrRight,
            SwitchProButton.Plus to Plus,
            SwitchProButton.Home to Home,
            SwitchProButton.LStickClick to RS,
        )
    }

    private fun sticks(side: JoyconSide): Map<SwitchProStick, StickSource> = when (side) {
        JoyconSide.DUAL -> mapOf(
            SwitchProStick.LStick to LEFT_STICK,
            SwitchProStick.RStick to RIGHT_STICK,
        )
        JoyconSide.LEFT -> mapOf(SwitchProStick.LStick to LEFT_STICK)
        JoyconSide.RIGHT -> mapOf(SwitchProStick.LStick to RIGHT_STICK)
    }
}
