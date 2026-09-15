package com.joegec.joycon2android.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.TextDim

/** Consent for the one destructive step auto setup needs: closing the emulator before writing. */
@Composable
fun CloseEmulatorDialog(
    emulatorName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text(
                stringResource(R.string.close_emulator_title, emulatorName),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
            )
        },
        text = {
            Text(
                stringResource(R.string.close_emulator_body, emulatorName),
                color = TextDim,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.close_emulator_confirm),
                    color = Accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close_emulator_cancel), color = TextDim)
            }
        },
    )
}
