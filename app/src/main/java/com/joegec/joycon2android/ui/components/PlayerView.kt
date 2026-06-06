package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun PlayerView(
    playerState: PlayerState,
    onUnassign: (String) -> Unit,
    onDisconnect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlayerHeader(playerState, onUnassign, onDisconnect)
        PlayerControllerLayout(playerState)
    }
}

@Composable
private fun PlayerHeader(
    playerState: PlayerState,
    onUnassign: (String) -> Unit,
    onDisconnect: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.player_label, playerState.player.index),
            color = Accent,
            fontSize = Dimens.fontSizeButtonLarge,
            fontWeight = FontWeight.Bold,
        )
        Row {
            playerState.left?.let {
                TextButton(onClick = { onUnassign(it.address) }) {
                    Text(stringResource(R.string.button_unassign_left), color = TextDim, fontSize = Dimens.fontSizeSmall)
                }
                TextButton(onClick = { onDisconnect(it.address) }) {
                    Text(stringResource(R.string.button_disconnect_left), color = TextDim, fontSize = Dimens.fontSizeSmall)
                }
            }
            playerState.right?.let {
                TextButton(onClick = { onUnassign(it.address) }) {
                    Text(stringResource(R.string.button_unassign_right), color = TextDim, fontSize = Dimens.fontSizeSmall)
                }
                TextButton(onClick = { onDisconnect(it.address) }) {
                    Text(stringResource(R.string.button_disconnect_right), color = TextDim, fontSize = Dimens.fontSizeSmall)
                }
            }
        }
    }
}
