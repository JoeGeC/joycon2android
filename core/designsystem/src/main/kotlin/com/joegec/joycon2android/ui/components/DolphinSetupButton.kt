package com.joegec.joycon2android.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.ErrorText
import com.joegec.joycon2android.ui.theme.TextOnAccent

/** Filled accent button (Scan-button styling) that runs a one-shot emulator config write. */
@Composable
fun DolphinSetupButton(
    phase: DolphinSetupPhase,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToastOnSetupSuccess(phase)
    Button(
        onClick = onClick,
        modifier = modifier.height(Dimens.buttonHeight),
        enabled = phase != DolphinSetupPhase.WORKING,
        shape = RoundedCornerShape(Dimens.buttonCorner),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            disabledContainerColor = Accent.copy(alpha = WORKING_ALPHA),
        ),
    ) {
        if (phase == DolphinSetupPhase.WORKING) {
            val workingDescription = stringResource(R.string.dolphin_setup_working)
            CircularProgressIndicator(
                modifier = Modifier
                    .size(Dimens.iconSizeMedium)
                    .semantics { contentDescription = workingDescription },
                color = TextOnAccent,
                strokeWidth = 2.dp,
            )
        } else {
            Text(label, color = TextOnAccent, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// Keyed to the WORKING → SUCCESS transition rather than SUCCESS itself, which lingers in the
// ViewModel and would re-toast whenever the card re-enters composition.
@Composable
private fun ToastOnSetupSuccess(phase: DolphinSetupPhase) {
    val context = LocalContext.current
    var previousPhase by remember { mutableStateOf(phase) }
    LaunchedEffect(phase) {
        if (previousPhase == DolphinSetupPhase.WORKING && phase == DolphinSetupPhase.SUCCESS) {
            Toast.makeText(context, R.string.dolphin_setup_done, Toast.LENGTH_LONG).show()
        }
        previousPhase = phase
    }
}

private const val WORKING_ALPHA = 0.6f

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
