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

@Composable
internal fun OutputDevice.label(): String = stringResource(
    when (this) {
        OutputDevice.PAD -> R.string.output_device_pad
        OutputDevice.MOTION -> R.string.output_device_motion
        OutputDevice.KEYBOARD -> R.string.output_device_keys
        OutputDevice.MOUSE -> R.string.output_device_mouse
    },
)

@Composable
internal fun EmulatorTarget.label(): String = name.orEmpty()
