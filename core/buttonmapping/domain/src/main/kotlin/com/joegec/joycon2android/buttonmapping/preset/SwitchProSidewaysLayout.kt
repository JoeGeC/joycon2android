package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.PRO_CONTROLLER_KEY
import com.joegec.joycon2android.buttonmapping.PRO_CONTROLLER_ON
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.directionKey
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconButton.A
import com.joegec.joycon2android.model.JoyconButton.B
import com.joegec.joycon2android.model.JoyconButton.Capture
import com.joegec.joycon2android.model.JoyconButton.Down
import com.joegec.joycon2android.model.JoyconButton.Home
import com.joegec.joycon2android.model.JoyconButton.LS
import com.joegec.joycon2android.model.JoyconButton.Left
import com.joegec.joycon2android.model.JoyconButton.Minus
import com.joegec.joycon2android.model.JoyconButton.Plus
import com.joegec.joycon2android.model.JoyconButton.RS
import com.joegec.joycon2android.model.JoyconButton.Right
import com.joegec.joycon2android.model.JoyconButton.SlLeft
import com.joegec.joycon2android.model.JoyconButton.SlRight
import com.joegec.joycon2android.model.JoyconButton.SrLeft
import com.joegec.joycon2android.model.JoyconButton.SrRight
import com.joegec.joycon2android.model.JoyconButton.Up
import com.joegec.joycon2android.model.JoyconButton.X
import com.joegec.joycon2android.model.JoyconButton.Y

/** A lone Joy-Con as a Pro Controller held sideways: docs/virtual-gamepad.md#sidewaysmapper */
sealed class SwitchProSidewaysLayout(
    override val id: String,
    private val slTarget: SwitchProButton,
    private val srTarget: SwitchProButton,
) : MappingPreset {
    override val console = Console.SWITCH_PRO
    override val sides = setOf(JoyconSide.LEFT, JoyconSide.RIGHT)

    // Every target cleared first, so the Standard bindings layered beneath can't fire too.
    override fun entries(side: JoyconSide) =
        cleared() + buttons(side).buttonEntries() + sticks(side).stickEntries() + (PRO_CONTROLLER_KEY to PRO_CONTROLLER_ON)

    private fun cleared(): Map<String, String> =
        SwitchProButton.entries.associate { it.name to "" } +
            SwitchProStick.entries.flatMap { stick -> StickDirection.entries.map { stick.directionKey(it) to "" } }

    private fun buttons(side: JoyconSide): Map<SwitchProButton, JoyconButton> = when (side) {
        JoyconSide.LEFT -> mapOf(
            SwitchProButton.A to Down,
            SwitchProButton.B to Left,
            SwitchProButton.X to Right,
            SwitchProButton.Y to Up,
            slTarget to SlLeft,
            srTarget to SrLeft,
            SwitchProButton.Plus to Minus,
            SwitchProButton.LStickClick to LS,
            SwitchProButton.Capture to Capture,
        )
        else -> mapOf(
            SwitchProButton.A to X,
            SwitchProButton.B to A,
            SwitchProButton.X to Y,
            SwitchProButton.Y to B,
            slTarget to SlRight,
            srTarget to SrRight,
            SwitchProButton.Plus to Plus,
            SwitchProButton.Home to Home,
            SwitchProButton.LStickClick to RS,
        )
    }

    private fun sticks(side: JoyconSide): Map<SwitchProStick, StickSource> =
        mapOf(SwitchProStick.LStick to if (side == JoyconSide.LEFT) StickSource.LEFT_STICK else StickSource.RIGHT_STICK)
}
