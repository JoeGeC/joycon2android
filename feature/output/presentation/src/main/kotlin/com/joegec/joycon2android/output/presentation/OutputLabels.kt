package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.DeviceOverride
import com.joegec.joycon2android.output.EmulatorKind
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputMode

@Composable
internal fun OutputMode.label(): String = when (this) {
    OutputMode.CONTROLLER -> stringResource(R.string.output_mode_controller)
    OutputMode.MOUSE_KEYBOARD -> stringResource(R.string.output_mode_mouse_keyboard)
}

/** An installed emulator by its own name; the others by what they run. */
@Composable
internal fun EmulatorTarget.label(): String = name ?: when (kind) {
    EmulatorKind.OTHER_GAMEPAD -> stringResource(R.string.output_other_gamepad)
    EmulatorKind.OTHER_MOTION -> stringResource(R.string.output_other_motion)
    EmulatorKind.DOLPHIN_GAMECUBE, EmulatorKind.DOLPHIN_WII, EmulatorKind.EDEN -> id
}

/** Under each other target's name in the picker, so it reads as a use, not a device. */
@Composable
internal fun EmulatorTarget.useNote(): String? = when (kind) {
    EmulatorKind.OTHER_GAMEPAD -> stringResource(R.string.output_other_gamepad_use)
    EmulatorKind.OTHER_MOTION -> stringResource(R.string.output_other_motion_use)
    EmulatorKind.DOLPHIN_GAMECUBE, EmulatorKind.DOLPHIN_WII, EmulatorKind.EDEN -> null
}

@Composable
internal fun OutputDevice.label(): String = when (this) {
    OutputDevice.PAD -> stringResource(R.string.output_device_pad)
    OutputDevice.MOTION -> stringResource(R.string.output_device_motion)
    OutputDevice.KEYBOARD -> stringResource(R.string.output_device_keyboard)
    OutputDevice.MOUSE -> stringResource(R.string.output_device_mouse)
}

@Composable
internal fun DeviceOverride.label(): String = when (this) {
    DeviceOverride.AUTO -> stringResource(R.string.output_override_auto)
    DeviceOverride.ON -> stringResource(R.string.output_override_on)
    DeviceOverride.OFF -> stringResource(R.string.output_override_off)
}
