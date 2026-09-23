package com.joegec.joycon2android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.core.designsystem.R

@Composable
fun CloseEmulatorDialog(
    emulatorName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmDialog(
        title = stringResource(R.string.close_emulator_title, emulatorName),
        body = stringResource(R.string.close_emulator_body, emulatorName),
        confirmLabel = stringResource(R.string.close_emulator_confirm),
        dismissLabel = stringResource(R.string.close_emulator_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
