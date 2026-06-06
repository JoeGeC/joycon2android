package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
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
    onAssign: (String, PlayerNumber) -> Unit,
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
            JoyconAssignmentRow(joycon, onAssign)
        }
    }
}

@Composable
private fun JoyconAssignmentRow(
    joycon: ConnectedJoycon,
    onAssign: (String, PlayerNumber) -> Unit,
) {
    val sideColor = when (joycon.side) {
        Side.LEFT -> LeftJoyconColor
        Side.RIGHT -> RightJoyconColor
        else -> TextDim
    }
    val sideLabel = when (joycon.side) {
        Side.LEFT -> stringResource(R.string.side_left)
        Side.RIGHT -> stringResource(R.string.side_right)
        else -> joycon.deviceName
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            sideLabel,
            color = sideColor,
            fontSize = Dimens.fontSizeBody,
            fontWeight = FontWeight.Medium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlayerNumber.entries.forEach { player ->
                FilterChip(
                    selected = false,
                    onClick = { onAssign(joycon.address, player) },
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
