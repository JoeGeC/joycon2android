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
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
import com.joegec.joycon2android.ui.components.LabeledDropdown
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun ControllerMappingScreen(
    console: Console,
    leftMapping: Map<String, String>,
    rightMapping: Map<String, String>,
    dualMapping: Map<String, String>,
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
            MappingRow(label, mapping[key] ?: MappingOptions.NONE_ID, sourceOptions) { onSetMapping(side, key, it) }
        }
        TextButton(onClick = { onResetMapping(side) }) {
            Text(stringResource(R.string.controller_mapping_reset))
        }
    }
}

@Composable
private fun MappingRow(
    label: String,
    selectedId: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextDim, modifier = Modifier.weight(1f))
        LabeledDropdown(
            options = options,
            selectedId = selectedId,
            onSelect = onSelect,
            modifier = Modifier.weight(1f),
        )
    }
}
