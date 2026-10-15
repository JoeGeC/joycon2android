package com.joegec.joycon2android.buttonmapping.target

import com.joegec.joycon2android.buttonmapping.JoyconSide

/** A single Joy-Con has only its own half's buttons, plus SL/SR, which a Pro Controller lacks. */
object SwitchProTargets {

    fun buttons(side: JoyconSide, singleJoycon: Boolean): List<SwitchProButton> = when {
        !singleJoycon -> SwitchProButton.entries - RAILS
        side == JoyconSide.LEFT -> LEFT_JOYCON
        else -> RIGHT_JOYCON
    }

    fun sticks(side: JoyconSide, singleJoycon: Boolean): List<SwitchProStick> = when {
        !singleJoycon -> SwitchProStick.entries
        side == JoyconSide.LEFT -> listOf(SwitchProStick.LStick)
        else -> listOf(SwitchProStick.RStick)
    }

    private val RAILS = setOf(SwitchProButton.SL, SwitchProButton.SR)

    private val LEFT_JOYCON = listOf(
        SwitchProButton.DPadUp, SwitchProButton.DPadDown, SwitchProButton.DPadLeft, SwitchProButton.DPadRight,
        SwitchProButton.L, SwitchProButton.ZL, SwitchProButton.Minus, SwitchProButton.LStickClick,
        SwitchProButton.Capture, SwitchProButton.SL, SwitchProButton.SR,
    )

    private val RIGHT_JOYCON = listOf(
        SwitchProButton.A, SwitchProButton.B, SwitchProButton.X, SwitchProButton.Y,
        SwitchProButton.R, SwitchProButton.ZR, SwitchProButton.Plus, SwitchProButton.RStickClick,
        SwitchProButton.Home, SwitchProButton.SL, SwitchProButton.SR,
    )
}
