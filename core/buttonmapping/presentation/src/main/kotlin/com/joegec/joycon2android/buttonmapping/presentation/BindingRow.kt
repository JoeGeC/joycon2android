package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.joegec.joycon2android.buttonmapping.sourceIdOf
import com.joegec.joycon2android.buttonmapping.sourceIdsOf
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
import com.joegec.joycon2android.ui.components.MultiSelectDropdown
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.SectionBg
import com.joegec.joycon2android.ui.theme.TextDim

/** A section on its own panel, its header tapped to show or hide its rows. */
@Composable
internal fun MappingSectionBox(section: MappingSection, open: Boolean, content: @Composable () -> Unit) {
    ExpandableInfoSection(
        section.label(),
        Modifier
            .clip(RoundedCornerShape(Dimens.buttonCorner))
            .background(SectionBg),
        initiallyExpanded = open,
        insidePadding = Dimens.mappingSectionPadding,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) { content() }
    }
}

/** One target and every source bound to it, stored as [targetKey]. A tap listens for a press first; a press replaces them all. */
@Composable
internal fun BindingRow(
    label: String,
    targetKey: String,
    sources: List<Pair<String, String>>,
    state: PlayerMappingUiState,
    controller: LiveController,
    actions: MappingActions,
) {
    val selectedIds = sourceIdsOf(state.mapping[targetKey].orEmpty())
    var listening by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextDim, modifier = Modifier.weight(1f))
        MultiSelectDropdown(
            options = sources,
            selectedIds = selectedIds,
            onToggle = { toggled ->
                actions.setMapping(state.body, targetKey, sourceIdOf(selectedIds.toggling(toggled)))
            },
            expanded = picking,
            onExpandedChange = { open -> if (open) listening = true else picking = false },
            modifier = Modifier.weight(1f),
        )
    }
    if (listening) {
        PressToMapDialog(
            targetLabel = label,
            controller = controller,
            side = state.body.side,
            heldSideways = state.wiiController.heldSideways,
            offeredIds = sources.mapTo(mutableSetOf()) { it.first },
            pauseDeviceKeys = actions.setDeviceKeysPaused,
            onPressed = { sourceId ->
                listening = false
                actions.setMapping(state.body, targetKey, sourceIdOf(listOf(sourceId)))
                actions.confirmPress(controller.player)
            },
            onChooseFromList = {
                listening = false
                picking = true
            },
            onDismiss = { listening = false },
        )
    }
}

/** Any source can fire a target, so picking one adds it; picking "None" empties the row. */
private fun List<String>.toggling(sourceId: String): List<String> = when {
    sourceId == MappingOptions.NONE_ID -> emptyList()
    sourceId in this -> this - sourceId
    else -> this + sourceId
}
