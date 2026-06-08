package com.joegec.joycon2android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.LeftJoyconColor
import com.joegec.joycon2android.ui.theme.RightJoyconColor
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
internal fun AssignmentPanel(
    unassigned: List<ConnectedJoycon>,
    players: List<PlayerState>,
    onAssign: (String, PlayerNumber) -> Unit,
    onDisconnect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.cardCorner)

    Column(
        modifier
            .fillMaxWidth()
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, Accent.copy(alpha = 0.3f), shape)
            .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.unassigned_title),
            color = Accent,
            fontSize = Dimens.fontSizeButton,
            fontWeight = FontWeight.Bold,
        )

        unassigned.forEach { joycon ->
            JoyconAssignmentRow(joycon, players, onAssign, onDisconnect)
        }
    }
}

@Composable
private fun JoyconAssignmentRow(
    joycon: ConnectedJoycon,
    players: List<PlayerState>,
    onAssign: (String, PlayerNumber) -> Unit,
    onDisconnect: (String) -> Unit,
) {
    val sideColor = when (joycon.side) {
        Side.LEFT -> LeftJoyconColor
        Side.RIGHT -> RightJoyconColor
        Side.PRO -> Color.White
        Side.UNKNOWN -> TextDim
    }
    val sideLabel = when (joycon.side) {
        Side.LEFT -> stringResource(R.string.side_left)
        Side.RIGHT -> stringResource(R.string.side_right)
        Side.PRO -> stringResource(R.string.side_pro)
        Side.UNKNOWN -> joycon.deviceName
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    sideLabel,
                    color = sideColor,
                    fontSize = Dimens.fontSizeBody,
                    fontWeight = FontWeight.Medium,
                )
                AnimatedVisibility(
                    visible = !joycon.ready,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = sideColor,
                            strokeWidth = 2.dp,
                        )
                        Text(
                            stringResource(R.string.status_connecting),
                            color = TextDim,
                            fontSize = Dimens.fontSizeSmall,
                        )
                    }
                }
            }
            IconButton(
                onClick = { onDisconnect(joycon.address) },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.button_disconnect),
                    tint = TextDim,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            PlayerNumber.entries.forEach { player ->
                val slotTaken = isSlotTaken(joycon.side, player, players)
                FilterChip(
                    selected = false,
                    onClick = { onAssign(joycon.address, player) },
                    enabled = joycon.ready && !slotTaken,
                    label = {
                        Text(
                            stringResource(R.string.player_label, player.index),
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CardBg,
                        labelColor = TextDim,
                        selectedContainerColor = Accent,
                        selectedLabelColor = TextOnAccent,
                    ),
                )
            }
        }
    }
}

private fun isSlotTaken(side: Side, player: PlayerNumber, players: List<PlayerState>): Boolean {
    val playerState = players.find { it.player == player } ?: return false
    if (playerState.hasPro) return true
    return when (side) {
        Side.LEFT -> playerState.left != null
        Side.RIGHT -> playerState.right != null
        Side.PRO -> playerState.hasController
        Side.UNKNOWN -> playerState.left != null && playerState.right != null
    }
}
