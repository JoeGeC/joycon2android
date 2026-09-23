package com.joegec.joycon2android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.core.designsystem.R

/** An emulator only reads its config on start. */
@Composable
fun StartEmulatorDialog(
    emulatorName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmDialog(
        title = stringResource(R.string.start_emulator_title, emulatorName),
        body = stringResource(R.string.start_emulator_body, emulatorName),
        confirmLabel = stringResource(R.string.start_emulator_confirm),
        dismissLabel = stringResource(R.string.start_emulator_dismiss),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
