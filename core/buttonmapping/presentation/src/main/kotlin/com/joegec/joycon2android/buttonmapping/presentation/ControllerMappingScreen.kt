package com.joegec.joycon2android.buttonmapping.presentation

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
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.target.GameCubeButton
import com.joegec.joycon2android.buttonmapping.target.GameCubeStick
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
import com.joegec.joycon2android.ui.components.LabeledDropdown
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

private const val NONE_ID = ""

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
        val buttonOptions = physicalButtonOptions(side)
        buttonTargetsFor(console).forEach { (key, label) ->
            MappingRow(label, mapping[key] ?: NONE_ID, buttonOptions) { onSetMapping(side, key, it) }
        }
        if (side == JoyconSide.DUAL) {
            val stickOptions = StickSource.entries.map { it.name to it.displayName }
            stickTargetsFor(console).forEach { (key, label) ->
                MappingRow(label, mapping[key] ?: stickOptions.first().first, stickOptions) { onSetMapping(side, key, it) }
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

private fun buttonTargetsFor(console: Console): List<Pair<String, String>> = when (console) {
    Console.GAMECUBE -> GameCubeButton.entries.map { it.name to it.displayName }
    Console.WIIMOTE_NUNCHUK -> WiimoteButton.entries.map { it.name to it.displayName }
    Console.SWITCH_PRO -> SwitchProButton.entries.map { it.name to it.displayName }
}

private fun stickTargetsFor(console: Console): List<Pair<String, String>> = when (console) {
    Console.GAMECUBE -> GameCubeStick.entries.map { it.name to it.displayName }
    Console.WIIMOTE_NUNCHUK -> WiimoteStick.entries.map { it.name to it.displayName }
    Console.SWITCH_PRO -> SwitchProStick.entries.map { it.name to it.displayName }
}

// The buttons a real, lone Joy-Con of that side can actually produce — matches what the physical
// hardware has, so a mapping chosen here can always fire (see JoyconButton for the full set; SL/SR
// are split per side, A/B/X/Y/Home only exist on the right Joy-Con, the d-pad only on the left).
private fun physicalButtonOptions(side: JoyconSide): List<Pair<String, String>> {
    val none = NONE_ID to "None"
    val buttons = when (side) {
        JoyconSide.DUAL -> JoyconButton.entries
        JoyconSide.LEFT -> listOf(
            JoyconButton.L, JoyconButton.ZL, JoyconButton.Minus, JoyconButton.LS,
            JoyconButton.Up, JoyconButton.Down, JoyconButton.Left, JoyconButton.Right,
            JoyconButton.Camera, JoyconButton.SlLeft, JoyconButton.SrLeft,
        )
        JoyconSide.RIGHT -> listOf(
            JoyconButton.R, JoyconButton.ZR, JoyconButton.Plus, JoyconButton.RS,
            JoyconButton.A, JoyconButton.B, JoyconButton.X, JoyconButton.Y,
            JoyconButton.Home, JoyconButton.SrRight, JoyconButton.SlRight,
        )
    }
    return listOf(none) + buttons.map { it.name to it.id }
}
