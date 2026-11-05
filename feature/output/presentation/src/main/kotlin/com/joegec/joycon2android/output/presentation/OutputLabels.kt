package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.Playing

@Composable
internal fun Playing.label(): String = stringResource(
    when (this) {
        Playing.SWITCH -> R.string.playing_switch
        Playing.GAMECUBE -> R.string.playing_gamecube
        Playing.WII -> R.string.playing_wii
        Playing.OTHER_EMULATOR -> R.string.playing_other_emulator
        Playing.ANDROID -> R.string.playing_android
        Playing.PC -> R.string.playing_pc
    },
)

/** What it's played on, under the name in the picker. */
@Composable
internal fun Playing.examples(): String = stringResource(
    when (this) {
        Playing.SWITCH -> R.string.playing_switch_examples
        Playing.GAMECUBE, Playing.WII -> R.string.playing_dolphin_examples
        Playing.OTHER_EMULATOR -> R.string.playing_other_emulator_examples
        Playing.ANDROID -> R.string.playing_android_examples
        Playing.PC -> R.string.playing_pc_examples
    },
)

@Composable
internal fun OutputDevice.label(motionDetail: String?): String = when (this) {
    OutputDevice.PAD -> stringResource(R.string.output_device_pad)
    OutputDevice.MOTION ->
        motionDetail?.let { stringResource(R.string.output_device_motion_with, it) } ?: stringResource(R.string.output_device_motion)
    OutputDevice.KEYBOARD -> stringResource(R.string.output_device_keyboard)
    OutputDevice.MOUSE -> stringResource(R.string.output_device_mouse)
}

@Composable
internal fun EmulatorTarget.label(): String = name.orEmpty()
