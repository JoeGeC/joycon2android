package com.joegec.joycon2android.dsu.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.dsu.DsuCoverage
import com.joegec.joycon2android.dsu.DsuStatus
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.ui.components.WarningBox
import com.joegec.joycon2android.ui.theme.Motion

/** The server's port and how many emulators are listening, e.g. "DSU on port 26760 · waiting for an emulator". */
@Composable
fun dsuPortText(clientCount: Int): String =
    if (clientCount == 0) stringResource(R.string.dsu_subtitle_waiting, DsuConfig.PORT)
    else pluralStringResource(R.plurals.dsu_subtitle_on, clientCount, DsuConfig.PORT, clientCount)

/** Any player the server has no slot for, while it runs. */
@Composable
fun DsuSlotLimitWarning(status: DsuStatus, coverage: DsuCoverage) {
    val text = slotLimitText(coverage)
    AnimatedVisibility(
        visible = status.enabled && text != null,
        enter = fadeIn(Motion.easeOut()) + expandVertically(Motion.easeOut()),
        exit = fadeOut(Motion.easeOut()) + shrinkVertically(Motion.easeOut()),
    ) {
        WarningBox(text.orEmpty())
    }
}

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
