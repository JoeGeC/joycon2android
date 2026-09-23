package com.joegec.joycon2android.buttonmapping.presentation

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.MappingLayout
import com.joegec.joycon2android.buttonmapping.MappingLayouts
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.PlayerMapping
import com.joegec.joycon2android.buttonmapping.SavedLayout

data class ControllerMappingUiState(
    val console: Console,
    val global: GlobalMapping,
    val players: List<PlayerMappingUiState>,
)

data class PlayerMappingUiState(
    val body: PlayerBody,
    val layouts: List<MappingLayout>,
    val layout: MappingLayout?,
    val sidewaysRemote: Boolean,
    val offersSidewaysRemote: Boolean,
    val mapping: Map<String, String>,
)

internal fun controllerMappingUiState(
    console: Console,
    mapping: GlobalMapping,
    savedLayouts: List<SavedLayout>,
) = ControllerMappingUiState(
    console = console,
    global = mapping,
    players = mapping.players.map {
        it.uiState(console, MappingLayouts.forBody(console, it.body.side, savedLayouts))
    },
)

private fun PlayerMapping.uiState(console: Console, layouts: List<MappingLayout>) = PlayerMappingUiState(
    body = body,
    layouts = layouts,
    layout = layout,
    sidewaysRemote = sidewaysRemote,
    offersSidewaysRemote = MappingOptions.offersSidewaysRemote(console, body.side),
    mapping = entries,
)

internal fun ControllerMappingUiState.takenNames(session: Boolean): List<String> =
    if (session) global.savedLayouts.map { it.name }
    else players.flatMap { it.layouts }.filterIsInstance<SavedLayout>().map { it.name }.distinct()
