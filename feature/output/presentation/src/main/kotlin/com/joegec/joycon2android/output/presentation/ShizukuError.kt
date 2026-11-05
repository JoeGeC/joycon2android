package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/** The pad, keyboard and mouse wait for Shizuku rather than fail, so they raise no error of their own. */
@Composable
internal fun shizukuError(state: OutputCardState): String? {
    if (!state.on || state.shizukuAvailable) return null
    return stringResource(if (state.playing.buttonsOverDsu) R.string.output_dsu_only else R.string.output_no_privileged_access)
}
