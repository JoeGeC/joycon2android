package com.joegec.joycon2android.dsu.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.dsu.DsuCoverage
import com.joegec.joycon2android.dsu.DsuStatus
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.ui.components.WarningBox
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.Motion
import com.joegec.joycon2android.ui.theme.TextDim

/** The DSU server's port and clients, and any player it has no slot for, while it runs. */
@Composable
fun DsuOutputStatus(status: DsuStatus, coverage: DsuCoverage) {
    AnimatedVisibility(
        visible = status.enabled,
        enter = fadeIn(Motion.easeOut()) + expandVertically(Motion.easeOut()),
        exit = fadeOut(Motion.easeOut()) + shrinkVertically(Motion.easeOut()),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
            Text(clientsText(status.clientCount), color = TextDim, style = MaterialTheme.typography.bodySmall)
            slotLimitText(coverage)?.let { WarningBox(it) }
        }
    }
}

@Composable
private fun clientsText(clientCount: Int): String =
    if (clientCount == 0) stringResource(R.string.dsu_subtitle_waiting, DsuConfig.PORT)
    else pluralStringResource(R.plurals.dsu_subtitle_on, clientCount, DsuConfig.PORT, clientCount)

@Composable
private fun slotLimitText(coverage: DsuCoverage): String? {
    if (coverage.allStreamed) return null
    val lines = mutableListOf(stringResource(R.string.dsu_slot_limit_lead))
    if (coverage.unservedPlayers.isNotEmpty()) {
        lines += stringResource(R.string.dsu_slot_limit_players, playerLabels(coverage.unservedPlayers))
    }
    if (coverage.unservedSecondHands.isNotEmpty()) {
        lines += stringResource(R.string.dsu_slot_limit_second_hands, playerLabels(coverage.unservedSecondHands))
    }
    return lines.joinToString("\n")
}

@Composable
private fun playerLabels(players: List<PlayerNumber>): String {
    val template = stringResource(R.string.player_label)
    return players.joinToString { template.format(it.index) }
}
