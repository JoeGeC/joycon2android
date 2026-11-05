package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.OutputMode

/** What a tab's switch row says: docs/DESIGN.md#output-card */
@Composable
internal fun outputStatus(mode: OutputMode, state: OutputCardState): String {
    val status = baseStatus(mode, state)
    if (mode != OutputMode.MOUSE_KEYBOARD || !state.isLit(mode) || state.mouseOffPlayers.isEmpty()) return status
    val players = state.mouseOffPlayers.sortedBy { it.index }.map { stringResource(R.string.player_label, it.index) }.joinToString(", ")
    return stringResource(R.string.output_status_mouse_off, status, players)
}

@Composable
private fun baseStatus(mode: OutputMode, state: OutputCardState): String = when {
    mode == OutputMode.MOTION && mode in state.running && state.motionDetail != null ->
        stringResource(R.string.output_status_active_with, state.motionDetail)
    mode in state.running -> stringResource(R.string.output_status_active)
    mode == OutputMode.GAMEPAD && state.isLit(mode) && state.motionRumblesThroughPad ->
        stringResource(R.string.output_status_on_for_rumble, OutputMode.MOTION.label())
    mode == OutputMode.MOUSE_KEYBOARD && state.isLit(mode) && state.tabsMappingKeys.isNotEmpty() ->
        onForMappings(state.tabsMappingKeys)
    else -> stringResource(R.string.output_status_off)
}

@Composable
private fun onForMappings(tabs: Set<OutputMode>): String {
    val names = OutputMode.entries.filter { it in tabs }.map { it.label() }
    return if (names.size == 1) stringResource(R.string.output_status_on_for, names[0])
    else stringResource(R.string.output_status_on_for_two, names[0], names[1])
}
