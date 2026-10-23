package com.joegec.joycon2android.gamepad.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.ui.components.InfoSheet
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/** How to use the virtual gamepad where the app writes no config: Android games and other emulators. */
@Composable
fun GamepadSetupSheet(onDismiss: () -> Unit) {
    InfoSheet(title = stringResource(R.string.gamepad_setup_title), onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
            listOf(
                R.string.gamepad_setup_device,
                R.string.gamepad_setup_map,
                R.string.gamepad_setup_order,
                R.string.gamepad_setup_motion,
            ).forEach { Text(stringResource(it), color = TextDim, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
