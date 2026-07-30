package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.connection.presentation.R
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
fun PlayerView(
    playerState: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val controllers = listOfNotNull(playerState.left, playerState.right)
    Column(modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.player_label, playerState.player.index),
            color = Accent,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.buttonCorner))
                .clickable { controllers.forEach { onUnassign(it.address) } }
                .padding(all = Dimens.compactChipPaddingVertical),
        )
        PlayerControllerLayout(playerState, onUnassign)
    }
}
