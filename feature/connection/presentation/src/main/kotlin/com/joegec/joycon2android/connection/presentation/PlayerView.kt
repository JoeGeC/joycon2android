package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.connection.presentation.R
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun PlayerView(
    playerState: PlayerState,
    onUnassign: (String) -> Unit,
    onRemovePlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.buttonCorner))
                .clickable(onClick = onRemovePlayer)
                .padding(all = Dimens.compactChipPaddingVertical),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.player_label, playerState.player.index),
                color = Accent,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.remove_player),
                tint = TextDim,
                modifier = Modifier.size(Dimens.iconSizeSmall),
            )
        }
        PlayerControllerLayout(playerState, onUnassign)
    }
}
