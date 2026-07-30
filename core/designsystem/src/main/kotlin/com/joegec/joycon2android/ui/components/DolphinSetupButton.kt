package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.ErrorText

/** Compact text-button (Copy-button styling) that runs a one-shot Dolphin config write. */
@Composable
fun DolphinSetupButton(
    phase: DolphinSetupPhase,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = phase != DolphinSetupPhase.WORKING,
        contentPadding = PaddingValues(horizontal = Dimens.buttonHorizontalPadding),
    ) {
        if (phase == DolphinSetupPhase.WORKING) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.progressIndicatorSmall),
                color = Accent,
                strokeWidth = 2.dp,
            )
        }
        Text(
            when (phase) {
                DolphinSetupPhase.WORKING -> stringResource(R.string.dolphin_setup_working)
                DolphinSetupPhase.SUCCESS -> stringResource(R.string.dolphin_setup_done)
                else -> label
            },
            color = Accent,
            fontSize = Dimens.fontSizeSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

val DolphinSetupPhase.isFailure: Boolean
    get() = this == DolphinSetupPhase.FAILED || this == DolphinSetupPhase.NO_ACCESS

@Composable
fun DolphinSetupMessage(phase: DolphinSetupPhase, modifier: Modifier = Modifier) {
    val message = when (phase) {
        DolphinSetupPhase.NO_ACCESS -> R.string.dolphin_setup_no_access
        DolphinSetupPhase.FAILED -> R.string.dolphin_setup_failed
        else -> return
    }
    Text(
        stringResource(message),
        color = ErrorText,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
    )
}
