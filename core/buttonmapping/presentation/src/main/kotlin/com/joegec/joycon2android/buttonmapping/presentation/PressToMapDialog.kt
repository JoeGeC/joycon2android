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
import com.joegec.joycon2android.buttonmapping.sourcePressedSince
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextTitle

/** Binds [targetLabel] to the next button or stick the player presses, if [offeredIds] has it. Device keys pause meanwhile. */
@Composable
internal fun PressToMapDialog(
    targetLabel: String,
    controller: LiveController,
    side: JoyconSide,
    heldSideways: Boolean,
    offeredIds: Set<String>,
    pauseDeviceKeys: (Boolean) -> Unit,
    onPressed: (sourceId: String) -> Unit,
    onChooseFromList: () -> Unit,
    onDismiss: () -> Unit,
) {
    DisposableEffect(Unit) {
        pauseDeviceKeys(true)
        onDispose { pauseDeviceKeys(false) }
    }
    val inputs by controller.inputs.collectAsState()
    val now = controller.player.withLiveInput(inputs)
    var previous by remember { mutableStateOf(now) }
    val currentOnPressed by rememberUpdatedState(onPressed)
    LaunchedEffect(now) {
        now.sourcePressedSince(previous, side, heldSideways)?.id?.takeIf { it in offeredIds }?.let(currentOnPressed)
        previous = now
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text(stringResource(R.string.press_to_map_title, targetLabel), color = TextTitle, style = MaterialTheme.typography.titleSmall)
        },
        text = {
            Text(stringResource(R.string.press_to_map_body), color = TextDim, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(onClick = onChooseFromList) {
                Text(stringResource(R.string.press_to_map_choose), color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.controller_mapping_cancel), color = TextDim)
            }
        },
    )
}
