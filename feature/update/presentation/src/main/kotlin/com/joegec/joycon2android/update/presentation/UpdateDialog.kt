package com.joegec.joycon2android.update.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.update.AvailableUpdate
import com.joegec.joycon2android.update.InstallProgress
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.AccentDim
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.ErrorText
import com.joegec.joycon2android.ui.theme.TextBright
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun UpdateDialog(
    update: AvailableUpdate,
    progress: InstallProgress?,
    onInstall: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text(
                stringResource(R.string.update_title, update.version.toString()),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
            )
        },
        text = {
            Column {
                Highlights(update.highlights)
                InstallStatus(progress)
                Spacer(Modifier.height(Dimens.sectionSpacing))
                TextButton(onClick = onSkip) {
                    Text(stringResource(R.string.update_skip), color = TextDim)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onInstall, enabled = progress !is InstallProgress.Downloading) {
                Text(
                    stringResource(R.string.update_install),
                    color = if (progress is InstallProgress.Downloading) TextDim else Accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.update_not_now), color = TextDim)
            }
        },
    )
}

@Composable
private fun Highlights(highlights: List<String>) {
    Column {
        highlights.forEach { highlight ->
            Text(
                stringResource(R.string.update_highlight, highlight),
                color = TextBright,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(Dimens.elementSpacing))
        }
    }
}

@Composable
private fun InstallStatus(progress: InstallProgress?) {
    when (progress) {
        is InstallProgress.Downloading -> DownloadProgress(progress.percent)
        InstallProgress.NeedsPermission -> StatusLine(R.string.update_needs_permission, TextBright)
        InstallProgress.HandedOff -> StatusLine(R.string.update_handed_off, TextDim)
        InstallProgress.Failed -> StatusLine(R.string.update_failed, ErrorText)
        null -> Unit
    }
}

@Composable
private fun DownloadProgress(percent: Int) {
    Spacer(Modifier.height(Dimens.elementSpacing))
    LinearProgressIndicator(
        progress = { percent / PERCENT },
        modifier = Modifier.fillMaxWidth(),
        color = Accent,
        trackColor = AccentDim,
    )
    Spacer(Modifier.height(Dimens.elementSpacing))
    Text(
        stringResource(R.string.update_downloading, percent),
        color = TextDim,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun StatusLine(@StringRes text: Int, color: Color) {
    Spacer(Modifier.height(Dimens.elementSpacing))
    Text(stringResource(text), color = color, style = MaterialTheme.typography.bodySmall)
}

private const val PERCENT = 100f
