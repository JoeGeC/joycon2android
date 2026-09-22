package com.joegec.joycon2android.buttonmapping.presentation

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.directionKey
import com.joegec.joycon2android.buttonmapping.target.GameCubeButton
import com.joegec.joycon2android.buttonmapping.target.GameCubeStick
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.model.JoyconButton

/** The (storage key, label) rows and (source id, label) choices the mapping editor offers. */
internal object MappingOptions {
    const val NONE_ID = ""

    /** Only a lone Joy-Con standing in for a Wii Remote can be held sideways in the sense the switch means. */
    fun offersSidewaysRemote(console: Console, side: JoyconSide) =
        console == Console.WIIMOTE_NUNCHUK && side != JoyconSide.DUAL

    /** Every row the editor offers, in reading order: buttons, then sticks, then what is neither. */
    fun targets(console: Console): List<Pair<String, String>> =
        buttonTargets(console) + stickDirectionTargets(console) + motionTargets(console)

    private fun buttonTargets(console: Console): List<Pair<String, String>> = when (console) {
        Console.GAMECUBE -> GameCubeButton.entries.map { it.name to it.displayName }
        Console.WIIMOTE_NUNCHUK -> (WiimoteButton.entries - MOTION_TARGETS).map { it.name to it.displayName }
        Console.SWITCH_PRO -> SwitchProButton.entries.map { it.name to it.displayName }
    }

    // Shaking the remote is a motion of it rather than a button on it, so it sits below the sticks
    // instead of among the face buttons.
    private val MOTION_TARGETS = setOf(WiimoteButton.Shake)

    private fun motionTargets(console: Console): List<Pair<String, String>> =
        if (console == Console.WIIMOTE_NUNCHUK) MOTION_TARGETS.map { it.name to it.displayName } else emptyList()

    private fun stickDirectionTargets(console: Console): List<Pair<String, String>> {
        val sticks = when (console) {
            Console.GAMECUBE -> GameCubeStick.entries.map { it to it.displayName }
            Console.WIIMOTE_NUNCHUK -> WiimoteStick.entries.map { it to it.displayName }
            Console.SWITCH_PRO -> SwitchProStick.entries.map { it to it.displayName }
        }
        return sticks.flatMap { (stick, label) ->
            StickDirection.entries.map { direction ->
                stick.directionKey(direction) to "$label ${direction.displayName}"
            }
        }
    }

    fun sources(side: JoyconSide): List<Pair<String, String>> =
        listOf(NONE_ID to "None") + physicalButtons(side).map { it.name to it.id } + stickDirections(side)

    // A lone Joy-Con has one stick, so its directions need no "Left"/"Right" to tell them apart.
    private fun stickDirections(side: JoyconSide): List<Pair<String, String>> {
        val sticks = when (side) {
            JoyconSide.DUAL -> StickSource.entries
            JoyconSide.LEFT -> listOf(StickSource.LEFT_STICK)
            JoyconSide.RIGHT -> listOf(StickSource.RIGHT_STICK)
        }
        return sticks.flatMap(MappingSource::directionsOf).map { source ->
            val stickLabel = if (side == JoyconSide.DUAL) source.stick.displayName else "Stick"
            source.id to "$stickLabel ${source.direction.displayName}"
        }
    }

    // The buttons a real, lone Joy-Con of that side can actually produce — matches what the physical
    // hardware has, so a mapping chosen here can always fire (see JoyconButton for the full set; SL/SR
    // are split per side, A/B/X/Y/Home/C only exist on the right Joy-Con, the d-pad only on the left).
    private fun physicalButtons(side: JoyconSide): List<JoyconButton> = when (side) {
        JoyconSide.DUAL -> JoyconButton.entries
        JoyconSide.LEFT -> listOf(
            JoyconButton.L, JoyconButton.ZL, JoyconButton.Minus, JoyconButton.LS,
            JoyconButton.Up, JoyconButton.Down, JoyconButton.Left, JoyconButton.Right,
            JoyconButton.Capture, JoyconButton.SlLeft, JoyconButton.SrLeft,
        )
        JoyconSide.RIGHT -> listOf(
            JoyconButton.R, JoyconButton.ZR, JoyconButton.Plus, JoyconButton.RS,
            JoyconButton.A, JoyconButton.B, JoyconButton.X, JoyconButton.Y,
            JoyconButton.Home, JoyconButton.Chat, JoyconButton.SrRight, JoyconButton.SlRight,
        )
    }
}
