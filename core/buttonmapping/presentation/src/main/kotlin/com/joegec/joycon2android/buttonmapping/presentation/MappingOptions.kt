package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.targetKey
import com.joegec.joycon2android.buttonmapping.directionKey
import com.joegec.joycon2android.buttonmapping.target.GameCubeButton
import com.joegec.joycon2android.buttonmapping.target.GameCubeStick
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.JoyconButton

internal object MappingOptions {
    const val NONE_ID = ""

    fun offersSidewaysRemote(console: Console, side: JoyconSide) =
        console == Console.WIIMOTE_NUNCHUK && side.isLone

    fun offersRemoteMotion(console: Console) = console == Console.WIIMOTE_NUNCHUK

    @Composable
    fun targets(console: Console, section: MappingSection): List<Pair<String, String>> = when (section) {
        MappingSection.BUTTONS -> buttonTargets(console)
        MappingSection.STICKS -> stickDirectionTargets(console)
        MappingSection.MOTION -> motionTargets(console)
        MappingSection.ANDROID -> AndroidKey.entries.map { it.targetKey to it.label() }
    }

    @Composable
    private fun buttonTargets(console: Console): List<Pair<String, String>> = when (console) {
        Console.GAMECUBE -> GameCubeButton.entries.map { it.name to it.label() }
        Console.WIIMOTE_NUNCHUK -> (WiimoteButton.entries - MOTION_TARGETS).map { it.name to it.label() }
        Console.SWITCH_PRO -> SwitchProButton.entries.map { it.name to it.label() }
    }

    // Not buttons on the remote, so they get their own section.
    private val MOTION_TARGETS = setOf(WiimoteButton.Shake, WiimoteButton.Recenter)

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

    // An Android key fires on a button press, so it offers no stick directions.
    @Composable
    fun sources(side: JoyconSide, section: MappingSection): List<Pair<String, String>> =
        listOf(NONE_ID to stringResource(R.string.source_none)) +
            physicalButtons(side).map { it.name to it.markingOn(side) } +
            if (section == MappingSection.ANDROID) emptyList() else stickDirections(side)

    // A lone Joy-Con has one stick, so its directions need no "Left"/"Right" to tell them apart.
    @Composable
    private fun stickDirections(side: JoyconSide): List<Pair<String, String>> {
        val sticks = when (side) {
            JoyconSide.DUAL, JoyconSide.GAMECUBE -> StickSource.entries
            JoyconSide.LEFT -> listOf(StickSource.LEFT_STICK)
            JoyconSide.RIGHT -> listOf(StickSource.RIGHT_STICK)
        }
        return sticks.flatMap(MappingSource::directionsOf).map { source ->
            val stick =
                if (side.isLone) stringResource(R.string.stick_lone) else source.stick.label()
            source.id to stringResource(R.string.source_direction, stick, source.direction.label())
        }
    }

    // Only what that side physically has, so every choice can fire.
    private fun physicalButtons(side: JoyconSide): List<JoyconButton> = when (side) {
        JoyconSide.DUAL -> JoyconButton.entries
        JoyconSide.GAMECUBE -> listOf(
            JoyconButton.A, JoyconButton.B, JoyconButton.X, JoyconButton.Y,
            JoyconButton.L, JoyconButton.R, JoyconButton.ZL, JoyconButton.ZR, JoyconButton.Plus,
            JoyconButton.Up, JoyconButton.Down, JoyconButton.Left, JoyconButton.Right,
            JoyconButton.Home, JoyconButton.Capture, JoyconButton.Chat,
        )
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

    // The GameCube controller's Z reports as ZR.
    private fun JoyconButton.markingOn(side: JoyconSide): String =
        if (side == JoyconSide.GAMECUBE && this == JoyconButton.ZR) "Z" else id
}
