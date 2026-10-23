package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputMode

@Composable
internal fun OutputMode.label(): String = when (this) {
    OutputMode.GAMEPAD -> stringResource(R.string.output_mode_gamepad)
    OutputMode.MOTION -> stringResource(R.string.output_mode_motion)
    OutputMode.MOUSE_KEYBOARD -> stringResource(R.string.output_mode_mouse_keyboard)
}

@Composable
internal fun OutputDevice.label(): String = when (this) {
    OutputDevice.PAD -> stringResource(R.string.output_device_pad)
    OutputDevice.MOTION -> stringResource(R.string.output_device_motion)
    OutputDevice.KEYBOARD -> stringResource(R.string.output_device_keyboard)
    OutputDevice.MOUSE -> stringResource(R.string.output_device_mouse)
}

/** An installed emulator by its own name; [EmulatorTarget.OTHER] as Other. */
@Composable
internal fun EmulatorTarget.label(): String = name ?: stringResource(R.string.output_other)
