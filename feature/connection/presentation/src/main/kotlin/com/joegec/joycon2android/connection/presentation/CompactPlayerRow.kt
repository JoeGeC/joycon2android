package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.connection.presentation.R
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.JoyconDefaultColor
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.WarningText
import com.joegec.joycon2android.ui.theme.joyconBorderColor

@Composable
fun CompactPlayerRow(
    playerState: PlayerState,
    onUnassign: (String) -> Unit,
    onRemovePlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cardCorner))
            .background(CardBg)
            .clickable(onClick = onRemovePlayer)
            .padding(
                horizontal = Dimens.compactRowPaddingHorizontal,
                vertical = Dimens.compactRowPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
    ) {
        Text(
            stringResource(R.string.player_label, playerState.player.index),
            color = Accent,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.compactControllerGap)) {
            when {
                playerState.hasPro ->
                    ControllerChip(playerState.left!!.modelLabel(), playerState.left!!, onUnassign)
                playerState.hasFullController -> {
                    ControllerChip(stringResource(R.string.controller_left), playerState.left!!, onUnassign, batteryFirst = true)
                    ControllerChip(stringResource(R.string.controller_right), playerState.right!!, onUnassign)
                }
                else -> {
                    playerState.left?.let { ControllerChip(stringResource(R.string.controller_left), it, onUnassign) }
                    playerState.right?.let { ControllerChip(stringResource(R.string.controller_right), it, onUnassign) }
                }
            }
        }
        Icon(
            Icons.Filled.Close,
            contentDescription = stringResource(R.string.remove_player),
            tint = TextDim,
            modifier = Modifier.size(Dimens.iconSizeSmall),
        )
    }
}

@Composable
private fun ControllerChip(
    text: String,
    joycon: ConnectedJoycon,
    onUnassign: (String) -> Unit,
    batteryFirst: Boolean = false,
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(Dimens.pillCorner))
            .clickable { onUnassign(joycon.address) }
            .minimumInteractiveComponentSize()
            .padding(
                horizontal = Dimens.compactChipPaddingHorizontal,
                vertical = Dimens.compactChipPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
    ) {
        val battery = @Composable {
            if (joycon.connectionLost) {
                Icon(
                    Icons.Filled.LinkOff,
                    contentDescription = stringResource(R.string.connection_lost),
                    tint = WarningText,
                    modifier = Modifier.size(Dimens.iconSizeSmall),
                )
            } else {
                joycon.input.battery?.let { BatteryGlyph(it) }
            }
        }

        if (batteryFirst) battery()
        Text(
            text,
            color = joyconBorderColor(joycon.accentColor, JoyconDefaultColor),
            style = MaterialTheme.typography.titleMedium,
        )
        if (!batteryFirst) battery()
    }
}
