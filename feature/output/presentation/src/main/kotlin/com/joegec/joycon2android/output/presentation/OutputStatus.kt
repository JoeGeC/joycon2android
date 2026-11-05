package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.OutputDevice

/** What the card's switch row says: docs/DESIGN.md#output-card */
@Composable
internal fun outputStatus(state: OutputCardState): String {
    if (!state.on) return stringResource(R.string.output_status_off)
    val live = OutputDevice.entries.filter { it in state.liveDevices }
    if (live.isEmpty()) return stringResource(R.string.output_status_starting)
    val status = stringResource(R.string.output_status_on_for, live.map { it.label(state.motionDetail) }.joinToString(", "))
    if (OutputDevice.MOUSE !in state.liveDevices || state.mouseOffPlayers.isEmpty()) return status
    val players = state.mouseOffPlayers.sortedBy { it.index }.map { stringResource(R.string.player_label, it.index) }.joinToString(", ")
    return stringResource(R.string.output_status_mouse_off, status, players)
}
