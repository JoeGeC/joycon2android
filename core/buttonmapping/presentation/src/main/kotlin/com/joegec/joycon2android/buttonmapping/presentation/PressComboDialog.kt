package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.heldSources
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextTitle

/** Collects what the player holds until they let go of everything, then binds it all as one combo. Device keys pause meanwhile. */
@Composable
internal fun PressComboDialog(
    targetLabel: String,
    controller: LiveController,
    side: JoyconSide,
    heldSideways: Boolean,
    offeredIds: Set<String>,
    pauseDeviceKeys: (Boolean) -> Unit,
    onCombo: (sourceIds: List<String>) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    DisposableEffect(Unit) {
        pauseDeviceKeys(true)
        onDispose { pauseDeviceKeys(false) }
    }
    val inputs by controller.inputs.collectAsState()
    val now = controller.player.withLiveInput(inputs)
    var collected by remember { mutableStateOf(emptyList<String>()) }
    val currentOnCombo by rememberUpdatedState(onCombo)
    LaunchedEffect(now) {
        val held = now.heldSources(side, heldSideways).map { it.id }.filter { it in offeredIds }
        if (held.isEmpty() && collected.isNotEmpty()) currentOnCombo(collected)
        else collected = collected + (held - collected.toSet())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text(stringResource(R.string.press_to_map_title, targetLabel), color = TextTitle, style = MaterialTheme.typography.titleSmall)
        },
        text = {
            Text(stringResource(R.string.press_combo_body), color = TextDim, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.press_combo_clear), color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.controller_mapping_cancel), color = TextDim)
            }
        },
    )
}
