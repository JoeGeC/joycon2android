package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalLayout
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.target.ClassicButton
import com.joegec.joycon2android.buttonmapping.target.ClassicStick
import com.joegec.joycon2android.buttonmapping.target.GameCubeButton
import com.joegec.joycon2android.buttonmapping.target.GameCubeStick
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.MouseAction

@Composable
internal fun Console.label(): String = when (this) {
    Console.GAMECUBE -> stringResource(R.string.console_gamecube)
    Console.WIIMOTE_NUNCHUK -> stringResource(R.string.console_wiimote_nunchuk)
    Console.SWITCH_PRO -> stringResource(R.string.console_switch_pro)
    Console.ANDROID -> stringResource(R.string.console_android)
    Console.PC -> stringResource(R.string.console_pc)
}

/** A keys-only mapping has no layouts to try, so it says how to map by hand instead. */
@Composable
internal fun Console.tip(viaDsu: Boolean): String = stringResource(
    when (this) {
        Console.ANDROID -> if (viaDsu) R.string.mapping_manual_motion else R.string.mapping_manual_gamepad
        Console.GAMECUBE, Console.WIIMOTE_NUNCHUK, Console.SWITCH_PRO, Console.PC -> R.string.controller_mapping_tips
    },
)

/** The mapping screen's title; a keys-only mapping is named for the output it's on. */
@Composable
internal fun Console.title(viaDsu: Boolean): String = when (this) {
    Console.ANDROID -> stringResource(if (viaDsu) R.string.mapping_title_motion else R.string.mapping_title_gamepad)
    Console.GAMECUBE, Console.WIIMOTE_NUNCHUK, Console.SWITCH_PRO, Console.PC -> label()
}

/** Short enough to sit in a list of players, as in "P1 L, P2 R". */
@Composable
internal fun JoyconSide.shortLabel(): String = when (this) {
    JoyconSide.LEFT -> stringResource(R.string.side_left_short)
    JoyconSide.RIGHT -> stringResource(R.string.side_right_short)
    JoyconSide.DUAL -> stringResource(R.string.side_dual_short)
    JoyconSide.GAMECUBE -> stringResource(R.string.side_gamecube_short)
}

@Composable
internal fun GlobalLayout.playerSummary(): String = bodies
    .map { stringResource(R.string.player_body, it.body.player.index, it.body.side.shortLabel()) }
    .joinToString(", ")

@Composable
internal fun StickDirection.label(): String = when (this) {
    StickDirection.UP -> stringResource(R.string.direction_up)
    StickDirection.DOWN -> stringResource(R.string.direction_down)
    StickDirection.LEFT -> stringResource(R.string.direction_left)
    StickDirection.RIGHT -> stringResource(R.string.direction_right)
}

@Composable
internal fun StickSource.label(): String = when (this) {
    StickSource.LEFT_STICK -> stringResource(R.string.stick_left)
    StickSource.RIGHT_STICK -> stringResource(R.string.stick_right)
}

@Composable
internal fun GameCubeButton.label(): String = when (this) {
    GameCubeButton.A -> "A"
    GameCubeButton.B -> "B"
    GameCubeButton.X -> "X"
    GameCubeButton.Y -> "Y"
    GameCubeButton.Z -> "Z"
    GameCubeButton.Start -> stringResource(R.string.button_start)
    GameCubeButton.TriggerL -> "L"
    GameCubeButton.TriggerR -> "R"
    GameCubeButton.DPadUp -> stringResource(R.string.button_dpad_up)
    GameCubeButton.DPadDown -> stringResource(R.string.button_dpad_down)
    GameCubeButton.DPadLeft -> stringResource(R.string.button_dpad_left)
    GameCubeButton.DPadRight -> stringResource(R.string.button_dpad_right)
}

@Composable
internal fun GameCubeStick.label(): String = when (this) {
    GameCubeStick.MainStick -> stringResource(R.string.stick_main)
    GameCubeStick.CStick -> stringResource(R.string.stick_c)
}

@Composable
internal fun WiimoteButton.label(): String = when (this) {
    WiimoteButton.A -> "A"
    WiimoteButton.B -> "B"
    WiimoteButton.One -> "1"
    WiimoteButton.Two -> "2"
    WiimoteButton.Home -> stringResource(R.string.button_home)
    WiimoteButton.Plus -> "+"
    WiimoteButton.Minus -> "-"
    WiimoteButton.DPadUp -> stringResource(R.string.button_dpad_up)
    WiimoteButton.DPadDown -> stringResource(R.string.button_dpad_down)
    WiimoteButton.DPadLeft -> stringResource(R.string.button_dpad_left)
    WiimoteButton.DPadRight -> stringResource(R.string.button_dpad_right)
    WiimoteButton.NunchukC -> stringResource(R.string.button_nunchuk_c)
    WiimoteButton.NunchukZ -> stringResource(R.string.button_nunchuk_z)
    WiimoteButton.Shake -> stringResource(R.string.button_shake)
    WiimoteButton.Recenter -> stringResource(R.string.button_recenter)
}

@Composable
internal fun WiimoteStick.label(): String = when (this) {
    WiimoteStick.NunchukStick -> stringResource(R.string.stick_nunchuk)
}

@Composable
internal fun ClassicButton.label(): String = when (this) {
    ClassicButton.ClassicA -> "A"
    ClassicButton.ClassicB -> "B"
    ClassicButton.ClassicX -> "X"
    ClassicButton.ClassicY -> "Y"
    ClassicButton.ClassicL -> "L"
    ClassicButton.ClassicR -> "R"
    ClassicButton.ClassicZL -> "ZL"
    ClassicButton.ClassicZR -> "ZR"
    ClassicButton.ClassicPlus -> "+"
    ClassicButton.ClassicMinus -> "-"
    ClassicButton.ClassicHome -> stringResource(R.string.button_home)
    ClassicButton.ClassicDPadUp -> stringResource(R.string.button_dpad_up)
    ClassicButton.ClassicDPadDown -> stringResource(R.string.button_dpad_down)
    ClassicButton.ClassicDPadLeft -> stringResource(R.string.button_dpad_left)
    ClassicButton.ClassicDPadRight -> stringResource(R.string.button_dpad_right)
    ClassicButton.ClassicShake -> stringResource(R.string.button_shake)
    ClassicButton.ClassicRecenter -> stringResource(R.string.button_recenter)
}

@Composable
internal fun ClassicStick.label(): String = when (this) {
    ClassicStick.ClassicLeftStick -> stringResource(R.string.stick_left)
    ClassicStick.ClassicRightStick -> stringResource(R.string.stick_right)
}

@Composable
internal fun SwitchProButton.label(): String = when (this) {
    SwitchProButton.A -> "A"
    SwitchProButton.B -> "B"
    SwitchProButton.X -> "X"
    SwitchProButton.Y -> "Y"
    SwitchProButton.L -> "L"
    SwitchProButton.R -> "R"
    SwitchProButton.ZL -> "ZL"
    SwitchProButton.ZR -> "ZR"
    SwitchProButton.Plus -> "+"
    SwitchProButton.Minus -> "-"
    SwitchProButton.Home -> stringResource(R.string.button_home)
    SwitchProButton.Capture -> stringResource(R.string.button_capture)
    SwitchProButton.LStickClick -> stringResource(R.string.button_lstick_click)
    SwitchProButton.RStickClick -> stringResource(R.string.button_rstick_click)
    SwitchProButton.DPadUp -> stringResource(R.string.button_dpad_up)
    SwitchProButton.DPadDown -> stringResource(R.string.button_dpad_down)
    SwitchProButton.DPadLeft -> stringResource(R.string.button_dpad_left)
    SwitchProButton.DPadRight -> stringResource(R.string.button_dpad_right)
    SwitchProButton.SL -> "SL"
    SwitchProButton.SR -> "SR"
}

@Composable
internal fun SwitchProStick.label(): String = when (this) {
    SwitchProStick.LStick -> stringResource(R.string.stick_l)
    SwitchProStick.RStick -> stringResource(R.string.stick_r)
}

@Composable
internal fun MappingSection.label(): String = when (this) {
    MappingSection.BUTTONS -> stringResource(R.string.section_buttons)
    MappingSection.STICKS -> stringResource(R.string.section_sticks)
    MappingSection.RUMBLE -> stringResource(R.string.section_rumble)
    MappingSection.MOTION -> stringResource(R.string.section_motion)
    MappingSection.ANDROID -> stringResource(R.string.section_android)
    MappingSection.KEYBOARD -> stringResource(R.string.section_keyboard)
    MappingSection.MOUSE -> stringResource(R.string.section_mouse)
}

@Composable
internal fun AndroidKey.label(): String = when (this) {
    AndroidKey.HOME -> stringResource(R.string.android_key_home)
    AndroidKey.BACK -> stringResource(R.string.android_key_back)
    AndroidKey.SCREENSHOT -> stringResource(R.string.android_key_screenshot)
}

@Composable
internal fun MouseAction.label(): String = when (this) {
    MouseAction.LEFT_CLICK -> stringResource(R.string.mouse_left_click)
    MouseAction.RIGHT_CLICK -> stringResource(R.string.mouse_right_click)
    MouseAction.MIDDLE_CLICK -> stringResource(R.string.mouse_middle_click)
    MouseAction.SCROLL_UP -> stringResource(R.string.mouse_scroll_up)
    MouseAction.SCROLL_DOWN -> stringResource(R.string.mouse_scroll_down)
}
