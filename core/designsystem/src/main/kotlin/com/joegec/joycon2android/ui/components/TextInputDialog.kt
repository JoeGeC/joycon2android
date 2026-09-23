package com.joegec.joycon2android.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.TextDim

/** [defaultValue] stands if nothing is typed, and clears when the field is tapped. */
@Composable
fun TextInputDialog(
    title: String,
    fieldLabel: String,
    defaultValue: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(defaultValue) }
    var offering by rememberSaveable { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    offering = false
                    text = it
                },
                singleLine = true,
                label = { Text(fieldLabel) },
                modifier = Modifier.onFocusChanged { focus ->
                    if (focus.isFocused && offering) {
                        offering = false
                        text = ""
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = TextDim,
                    focusedLabelColor = Accent,
                    unfocusedLabelColor = TextDim,
                    cursorColor = Accent,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim().ifBlank { defaultValue }) }) {
                Text(confirmLabel, color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel, color = TextDim) }
        },
    )
}
