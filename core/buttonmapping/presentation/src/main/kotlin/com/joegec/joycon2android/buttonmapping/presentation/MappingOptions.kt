package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.JoyconButton

internal object MappingOptions {
    const val NONE_ID = ""

    fun offersSidewaysRemote(console: Console, side: JoyconSide) =
        console == Console.WIIMOTE_NUNCHUK && side != JoyconSide.DUAL

    @Composable
    fun targets(console: Console): List<Pair<String, String>> =
        buttonTargets(console) + stickDirectionTargets(console) + motionTargets(console)

    @Composable
    private fun buttonTargets(console: Console): List<Pair<String, String>> = when (console) {
        Console.GAMECUBE -> GameCubeButton.entries.map { it.name to it.label() }
        Console.WIIMOTE_NUNCHUK -> (WiimoteButton.entries - MOTION_TARGETS).map { it.name to it.label() }
        Console.SWITCH_PRO -> SwitchProButton.entries.map { it.name to it.label() }
    }

    // Shake is a motion, so it's listed after the sticks.
    private val MOTION_TARGETS = setOf(WiimoteButton.Shake)

    @Composable
    private fun motionTargets(console: Console): List<Pair<String, String>> =
        if (console == Console.WIIMOTE_NUNCHUK) MOTION_TARGETS.map { it.name to it.label() } else emptyList()

    @Composable
    private fun stickDirectionTargets(console: Console): List<Pair<String, String>> {
        val sticks = when (console) {
            Console.GAMECUBE -> GameCubeStick.entries.map { it to it.label() }
            Console.WIIMOTE_NUNCHUK -> WiimoteStick.entries.map { it to it.label() }
            Console.SWITCH_PRO -> SwitchProStick.entries.map { it to it.label() }
        }
        return sticks.flatMap { (stick, label) ->
            StickDirection.entries.map { direction ->
                stick.directionKey(direction) to stringResource(R.string.source_direction, label, direction.label())
            }
        }
    }

    @Composable
    fun sources(side: JoyconSide): List<Pair<String, String>> =
        listOf(NONE_ID to stringResource(R.string.source_none)) +
            physicalButtons(side).map { it.name to it.id } +
            stickDirections(side)

    // A lone Joy-Con has one stick, so its directions need no "Left"/"Right" to tell them apart.
    @Composable
    private fun stickDirections(side: JoyconSide): List<Pair<String, String>> {
        val sticks = when (side) {
            JoyconSide.DUAL -> StickSource.entries
            JoyconSide.LEFT -> listOf(StickSource.LEFT_STICK)
            JoyconSide.RIGHT -> listOf(StickSource.RIGHT_STICK)
        }
        return sticks.flatMap(MappingSource::directionsOf).map { source ->
            val stick =
                if (side == JoyconSide.DUAL) source.stick.label() else stringResource(R.string.stick_lone)
            source.id to stringResource(R.string.source_direction, stick, source.direction.label())
        }
    }

    // Only what that side physically has, so every choice can fire.
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
