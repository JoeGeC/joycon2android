package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.sourceIdOf
import com.joegec.joycon2android.buttonmapping.sourceIdsOf
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.MultiSelectDropdown
import com.joegec.joycon2android.ui.components.SettingWarning
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun MappingBindings(
    console: Console,
    state: PlayerMappingUiState,
    actions: MappingActions,
    androidKeysNeedShizuku: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing)) {
        MappingSection.entries.forEach { section ->
            val targets = MappingOptions.targets(console, section)
            if (targets.isNotEmpty()) {
                BindingSection(section, targets, state, actions, androidKeysNeedShizuku)
            }
        }
    }
}

@Composable
private fun BindingSection(
    section: MappingSection,
    targets: List<Pair<String, String>>,
    state: PlayerMappingUiState,
    actions: MappingActions,
    androidKeysNeedShizuku: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        Text(
            section.label(),
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = Dimens.mappingHeaderTopPadding),
        )
        if (section == MappingSection.ANDROID && androidKeysNeedShizuku) {
            SettingWarning(stringResource(R.string.android_keys_need_shizuku))
        }
        val sourceOptions = MappingOptions.sources(state.body.side, section)
        targets.forEach { (key, label) ->
            val selectedIds = sourceIdsOf(state.mapping[key].orEmpty())
            BindingRow(label, selectedIds, sourceOptions) { toggled ->
                actions.setMapping(state.body, key, sourceIdOf(selectedIds.toggling(toggled)))
            }
        }
    }
}

@Composable
private fun BindingRow(
    label: String,
    selectedIds: List<String>,
    options: List<Pair<String, String>>,
    onToggle: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextDim, modifier = Modifier.weight(1f))
        MultiSelectDropdown(
            options = options,
            selectedIds = selectedIds,
            onToggle = onToggle,
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
