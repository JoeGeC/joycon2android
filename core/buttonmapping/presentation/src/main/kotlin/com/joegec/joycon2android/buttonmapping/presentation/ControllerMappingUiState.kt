package com.joegec.joycon2android.buttonmapping.presentation

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.MappingLayout
import com.joegec.joycon2android.buttonmapping.MappingLayouts
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.PlayerMapping
import com.joegec.joycon2android.buttonmapping.SavedLayout
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.ui.components.DropdownOption

/** A null [layoutName] is the editor's way of saying the bindings no longer match any layout. */
data class ControllerMappingUiState(
    val console: Console,
    val global: GlobalLayoutUiState,
    val players: List<PlayerMappingUiState>,
    /** Every name already taken on this console, so a suggested one is never a duplicate. */
    val savedLayoutNames: List<String>,
)

data class GlobalLayoutUiState(
    val options: List<DropdownOption>,
    val selectedId: String?,
    val layoutName: String?,
    val playerSummary: String?,
    val savedNames: List<String>,
)

data class PlayerMappingUiState(
    val body: PlayerBody,
    val layoutOptions: List<DropdownOption>,
    val selectedLayoutId: String?,
    val layoutName: String?,
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
    global = mapping.uiState(console),
    players = mapping.players.map {
        it.uiState(console, MappingLayouts.forBody(console, it.body.side, savedLayouts))
    },
    savedLayoutNames = savedLayouts.map { it.displayName },
)

private fun GlobalMapping.uiState(console: Console) = GlobalLayoutUiState(
    options = MappingPresets.forConsole(console).map { DropdownOption(it.id, it.displayName) } +
        savedLayouts.map {
            DropdownOption(
                id = it.id,
                label = it.displayName,
                subLabel = it.playerSummary,
                available = it.fits(bodies),
                deletable = true,
            )
        },
    selectedId = selectedId,
    layoutName = displayName,
    playerSummary = playerSummary,
    savedNames = savedLayouts.map { it.displayName },
)

private fun PlayerMapping.uiState(console: Console, layouts: List<MappingLayout>) = PlayerMappingUiState(
    body = body,
    layoutOptions = layouts.map { DropdownOption(it.id, it.displayName, deletable = it is SavedLayout) },
    selectedLayoutId = layout?.id,
    layoutName = layout?.displayName,
    sidewaysRemote = sidewaysRemote,
    offersSidewaysRemote = MappingOptions.offersSidewaysRemote(console, body.side),
    mapping = entries,
)
