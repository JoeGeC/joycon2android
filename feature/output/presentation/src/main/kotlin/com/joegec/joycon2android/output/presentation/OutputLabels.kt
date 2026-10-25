package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.OutputMode

@Composable
internal fun OutputMode.label(): String = when (this) {
    OutputMode.GAMEPAD -> stringResource(R.string.output_mode_gamepad)
    OutputMode.MOTION -> stringResource(R.string.output_mode_motion)
    OutputMode.MOUSE_KEYBOARD -> stringResource(R.string.output_mode_mouse_keyboard)
}

/** An installed emulator by its own name; [EmulatorTarget.OTHER] as Other. */
@Composable
internal fun EmulatorTarget.label(): String = name ?: stringResource(R.string.output_other)
