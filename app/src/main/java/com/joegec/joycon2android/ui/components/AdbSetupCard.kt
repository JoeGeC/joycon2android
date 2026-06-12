package com.joegec.joycon2android.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.uhid.AdbState
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.ErrorText
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
fun AdbSetupCard(
    state: AdbSetupState,
    onEnableNotifications: () -> Unit,
    onStartPairing: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val connected = state.state == AdbState.CONNECTED

    Column(
        modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(Dimens.buttonCorner))
            .padding(Dimens.cardPadding)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.adb_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = Dimens.fontSizeBody,
                )
                Text(
                    stringResource(subtitleFor(state.state)),
                    color = if (connected) Accent else TextDim,
                    fontSize = Dimens.fontSizeSmall,
                )
            }
            if (state.state == AdbState.WORKING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Accent,
                    strokeWidth = 2.dp,
                )
            }
        }

        if (connected) {
            TextButton(onClick = onDisconnect) {
                Text(stringResource(R.string.adb_disconnect), color = TextDim)
            }
        } else if (!state.notificationsGranted) {
            // The pairing code can only be entered via a notification, so that comes first
            Spacer(Modifier.height(Dimens.elementSpacing))
            Text(
                stringResource(R.string.adb_notifications_required),
                color = ErrorText,
                fontSize = Dimens.fontSizeSmall,
            )
            Spacer(Modifier.height(Dimens.elementSpacing))
            Button(
                onClick = onEnableNotifications,
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
                shape = RoundedCornerShape(Dimens.buttonCorner),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Text(
                    stringResource(R.string.adb_enable_notifications),
                    color = TextOnAccent,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            Spacer(Modifier.height(Dimens.elementSpacing))
            Text(
                stringResource(R.string.adb_instructions),
                color = TextDim,
                fontSize = Dimens.fontSizeSmall,
            )
            Spacer(Modifier.height(Dimens.elementSpacing))
            Button(
                onClick = {
                    onStartPairing()
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                },
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
                shape = RoundedCornerShape(Dimens.buttonCorner),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Text(
                    stringResource(R.string.adb_pair_device),
                    color = TextOnAccent,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        AnimatedVisibility(
            visible = state.error != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Spacer(Modifier.height(Dimens.elementSpacing))
                Text(state.error ?: "", color = ErrorText, fontSize = Dimens.fontSizeSmall)
            }
        }
    }
}

private fun subtitleFor(state: AdbState): Int = when (state) {
    AdbState.CONNECTED -> R.string.adb_subtitle_connected
    AdbState.WORKING -> R.string.adb_subtitle_working
    AdbState.DISCONNECTED -> R.string.adb_subtitle_disconnected
}
