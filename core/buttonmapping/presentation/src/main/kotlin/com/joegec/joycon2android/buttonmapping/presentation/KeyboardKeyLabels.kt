package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.KeyboardKey

/** Letters, digits and F-keys are their keycap markings; the rest are named. */
@Composable
internal fun KeyboardKey.label(): String = when (this) {
    KeyboardKey.UP -> stringResource(R.string.keyboard_key_up)
    KeyboardKey.DOWN -> stringResource(R.string.keyboard_key_down)
    KeyboardKey.LEFT -> stringResource(R.string.keyboard_key_left)
    KeyboardKey.RIGHT -> stringResource(R.string.keyboard_key_right)
    KeyboardKey.ENTER -> stringResource(R.string.keyboard_key_enter)
    KeyboardKey.ESCAPE -> stringResource(R.string.keyboard_key_escape)
    KeyboardKey.SPACE -> stringResource(R.string.keyboard_key_space)
    KeyboardKey.TAB -> stringResource(R.string.keyboard_key_tab)
    KeyboardKey.BACKSPACE -> stringResource(R.string.keyboard_key_backspace)
    KeyboardKey.DELETE -> stringResource(R.string.keyboard_key_delete)
    KeyboardKey.PAGE_UP -> stringResource(R.string.keyboard_key_page_up)
    KeyboardKey.PAGE_DOWN -> stringResource(R.string.keyboard_key_page_down)
    KeyboardKey.LEFT_SHIFT -> stringResource(R.string.keyboard_key_shift)
    KeyboardKey.LEFT_CTRL -> stringResource(R.string.keyboard_key_ctrl)
    KeyboardKey.LEFT_ALT -> stringResource(R.string.keyboard_key_alt)
    KeyboardKey.A, KeyboardKey.B, KeyboardKey.C, KeyboardKey.D, KeyboardKey.E, KeyboardKey.F, KeyboardKey.G,
    KeyboardKey.H, KeyboardKey.I, KeyboardKey.J, KeyboardKey.K, KeyboardKey.L, KeyboardKey.M, KeyboardKey.N,
    KeyboardKey.O, KeyboardKey.P, KeyboardKey.Q, KeyboardKey.R, KeyboardKey.S, KeyboardKey.T, KeyboardKey.U,
    KeyboardKey.V, KeyboardKey.W, KeyboardKey.X, KeyboardKey.Y, KeyboardKey.Z,
    KeyboardKey.F1, KeyboardKey.F2, KeyboardKey.F3, KeyboardKey.F4, KeyboardKey.F5, KeyboardKey.F6,
    KeyboardKey.F7, KeyboardKey.F8, KeyboardKey.F9, KeyboardKey.F10, KeyboardKey.F11, KeyboardKey.F12 -> name
    KeyboardKey.DIGIT_1, KeyboardKey.DIGIT_2, KeyboardKey.DIGIT_3, KeyboardKey.DIGIT_4, KeyboardKey.DIGIT_5,
    KeyboardKey.DIGIT_6, KeyboardKey.DIGIT_7, KeyboardKey.DIGIT_8, KeyboardKey.DIGIT_9, KeyboardKey.DIGIT_0 ->
        name.removePrefix("DIGIT_")
}
