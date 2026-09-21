package com.joegec.joycon2android.buttonmapping.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.sourceIdOf
import com.joegec.joycon2android.buttonmapping.sourceIdsOf
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
import com.joegec.joycon2android.ui.components.LabeledDropdown
import com.joegec.joycon2android.ui.components.MultiSelectDropdown
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun ControllerMappingScreen(
    console: Console,
    presetId: String,
    sidewaysRemote: Boolean,
    leftMapping: Map<String, String>,
    rightMapping: Map<String, String>,
    dualMapping: Map<String, String>,
    onSelectPreset: (presetId: String) -> Unit,
    onSetSidewaysRemote: (enabled: Boolean) -> Unit,
    onSetMapping: (side: JoyconSide, targetKey: String, sourceId: String) -> Unit,
    onResetMapping: (side: JoyconSide) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    Column(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = Dimens.screenPaddingHorizontal),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.controller_mapping_back))
            }
            Text(console.displayName, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        Spacer(Modifier.height(Dimens.sectionSpacing))
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
        ) {
            PresetRow(console, presetId, onSelectPreset)
            SidewaysRemoteSwitch(console, sidewaysRemote, onSetSidewaysRemote)
            ExpandableInfoSection(JoyconSide.LEFT.displayName) {
                MappingSection(console, JoyconSide.LEFT, leftMapping, onSetMapping, onResetMapping)
            }
            ExpandableInfoSection(JoyconSide.RIGHT.displayName) {
                MappingSection(console, JoyconSide.RIGHT, rightMapping, onSetMapping, onResetMapping)
            }
            ExpandableInfoSection(JoyconSide.DUAL.displayName) {
                MappingSection(console, JoyconSide.DUAL, dualMapping, onSetMapping, onResetMapping)
            }
            Spacer(Modifier.height(Dimens.sectionSpacing))
        }
    }
}

/** Only consoles with a layout to choose between show the row. */
@Composable
private fun PresetRow(console: Console, presetId: String, onSelectPreset: (String) -> Unit) {
    val presets = MappingOptions.presets(console)
    if (presets.size < 2) return
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.controller_mapping_preset), color = TextDim, modifier = Modifier.weight(1f))
        LabeledDropdown(
            options = presets,
            selectedId = presetId,
            onSelect = onSelectPreset,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * A lone Joy-Con stands in for a Wii Remote held sideways: what a wheel game steers by, and what
 * turns its d-pad. A layout sets it; this is the user having the last word.
 */
@Composable
private fun SidewaysRemoteSwitch(console: Console, enabled: Boolean, onSetEnabled: (Boolean) -> Unit) {
    if (!MappingOptions.offersSidewaysRemote(console)) return
    SettingSwitch(
        title = stringResource(R.string.controller_mapping_sideways_remote),
        description = stringResource(R.string.controller_mapping_sideways_remote_description),
        checked = enabled,
        onCheckedChange = onSetEnabled,
    )
}

@Composable
private fun MappingSection(
    console: Console,
    side: JoyconSide,
    mapping: Map<String, String>,
    onSetMapping: (side: JoyconSide, targetKey: String, sourceId: String) -> Unit,
    onResetMapping: (side: JoyconSide) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        val sourceOptions = MappingOptions.sources(side)
        (MappingOptions.buttonTargets(console) + MappingOptions.stickDirectionTargets(console)).forEach { (key, label) ->
            val selectedIds = sourceIdsOf(mapping[key].orEmpty())
            MappingRow(label, selectedIds, sourceOptions) { toggled ->
                onSetMapping(side, key, sourceIdOf(selectedIds.toggling(toggled)))
            }
        }
        TextButton(onClick = { onResetMapping(side) }) {
            Text(stringResource(R.string.controller_mapping_reset))
        }
    }
}

@Composable
private fun MappingRow(
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
