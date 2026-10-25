package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.buttonmapping.sourceIdOf
import com.joegec.joycon2android.buttonmapping.sourceIdsOf
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
import com.joegec.joycon2android.ui.components.MultiSelectDropdown
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/** A section's header, tapped to show or hide its rows. */
@Composable
internal fun MappingSectionBox(section: MappingSection, open: Boolean, content: @Composable () -> Unit) {
    ExpandableInfoSection(section.label(), Modifier.padding(top = Dimens.mappingHeaderTopPadding), initiallyExpanded = open) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) { content() }
    }
}

/** One target and every source bound to it, stored as [targetKey] in the player's mapping. */
@Composable
internal fun BindingRow(
    label: String,
    targetKey: String,
    sources: List<Pair<String, String>>,
    state: PlayerMappingUiState,
    actions: MappingActions,
) {
    val selectedIds = sourceIdsOf(state.mapping[targetKey].orEmpty())
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
            modifier = Modifier.weight(1f),
        )
    }
}

/** Any source can fire a target, so picking one adds it; picking "None" empties the row. */
private fun List<String>.toggling(sourceId: String): List<String> = when {
    sourceId == MappingOptions.NONE_ID -> emptyList()
    sourceId in this -> this - sourceId
    else -> this + sourceId
}
