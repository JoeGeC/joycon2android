package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.DeviceKeyKind
import com.joegec.joycon2android.output.OutputMode

/** The keyboard, mouse and Android keys wait for Shizuku rather than fail, so they raise no error of their own. */
@Composable
fun shizukuError(state: OutputCardState): String? {
    if (!state.running || state.shizukuAvailable) return null
    return when (state.selection.mode) {
        OutputMode.MOUSE_KEYBOARD -> stringResource(R.string.output_no_privileged_access)
        OutputMode.MOTION -> state.mappedKeys.takeIf { it.isNotEmpty() }?.let { neededFor(it) }
        OutputMode.GAMEPAD -> null
    }
}

@Composable
private fun neededFor(kinds: Set<DeviceKeyKind>): String {
    val names = DeviceKeyKind.entries.filter { it in kinds }.map { it.label() }
    val list = when (names.size) {
        1 -> names[0]
        2 -> stringResource(R.string.output_list_two, names[0], names[1])
        else -> stringResource(R.string.output_list_three, names[0], names[1], names[2])
    }
    return stringResource(R.string.output_shizuku_needed_for, list)
}

@Composable
private fun DeviceKeyKind.label(): String = stringResource(
    when (this) {
        DeviceKeyKind.MOUSE -> R.string.output_key_kind_mouse
        DeviceKeyKind.KEYBOARD -> R.string.output_key_kind_keyboard
        DeviceKeyKind.ANDROID -> R.string.output_key_kind_android
    },
)
