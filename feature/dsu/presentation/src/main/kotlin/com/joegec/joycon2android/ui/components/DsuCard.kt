package com.joegec.joycon2android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.feature.dsu.presentation.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun DsuCard(
    state: DsuCardState,
    onToggle: (Boolean) -> Unit,
    onLanToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    FeatureToggleCard(
        title = stringResource(R.string.dsu_title),
        subtitle = if (state.enabled) {
            stringResource(R.string.dsu_subtitle_on, DsuConfig.PORT, state.clientCount)
        } else {
            stringResource(R.string.dsu_subtitle_off)
        },
        checked = state.enabled,
        error = state.error,
        onToggle = onToggle,
        modifier = modifier,
    ) {
        AnimatedVisibility(
            visible = state.enabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Spacer(Modifier.height(Dimens.elementSpacing))
                LanRow(state.lanEnabled, onLanToggle)
                if (state.showSlotLimitNote) {
                    Spacer(Modifier.height(Dimens.elementSpacing))
                    Text(
                        stringResource(R.string.dsu_slot_note),
                        color = TextDim,
                        fontSize = Dimens.fontSizeSmall,
                    )
                }
                Spacer(Modifier.height(Dimens.elementSpacing))
                EmulatorConnection(state.address)
                EmulatorGuides()
            }
        }
    }
}

@Composable
private fun LanRow(lanEnabled: Boolean, onLanToggle: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.dsu_lan_title),
                color = Color.White,
                fontSize = Dimens.fontSizeMedium,
            )
            Text(
                stringResource(R.string.dsu_lan_subtitle),
                color = TextDim,
                fontSize = Dimens.fontSizeSmall,
            )
        }
        Switch(
            checked = lanEnabled,
            onCheckedChange = onLanToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent,
            ),
        )
    }
}

@Composable
private fun EmulatorConnection(address: String?) {
    Column {
        Text(
            stringResource(R.string.dsu_setup_title),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = Dimens.fontSizeMedium,
        )
        Text(
            stringResource(R.string.dsu_setup_body),
            color = TextDim,
            fontSize = Dimens.fontSizeSmall,
        )
        Spacer(Modifier.height(Dimens.elementSpacing))
        if (address == null) {
            Text(
                stringResource(R.string.dsu_no_lan_address),
                color = TextDim,
                fontSize = Dimens.fontSizeSmall,
            )
        } else {
            CopyableCode(address)
        }
    }
}

// Per-emulator quirks live behind expandable sections; add new emulators here
@Composable
private fun EmulatorGuides() {
    ExpandableInfoSection(stringResource(R.string.dsu_dolphin_guide_title)) {
        DolphinGuide()
    }
    ExpandableInfoSection(stringResource(R.string.dsu_mapping_trouble_title)) {
        MappingTroubleshooting()
    }
}

@Composable
private fun DolphinGuide() {
    Column {
        GuideStep(stringResource(R.string.dsu_dolphin_desktop))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_dolphin_android_intro))
        Spacer(Modifier.height(Dimens.elementSpacing))
        CopyableCode(stringResource(R.string.dsu_dolphin_ini))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_dolphin_android_outro))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_dolphin_mapping))
    }
}

@Composable
private fun MappingTroubleshooting() {
    Column {
        GuideStep(stringResource(R.string.dsu_mapping_trouble_detect))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_mapping_names_intro))
        Spacer(Modifier.height(Dimens.elementSpacing))
        Ds4NameTable()
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_mapping_missing))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_mapping_trouble_gamepad))
    }
}

// DSU carries exactly the DS4 button set — these are protocol input names, not UI copy.
// SL/SR/Chat have no DSU slot (see dsu_mapping_missing); Camera rides the touchpad click.
private val DS4_BUTTON_NAMES = listOf(
    "A" to "Circle", "B" to "Cross",
    "X" to "Triangle", "Y" to "Square",
    "L" to "L1", "R" to "R1",
    "ZL" to "L2", "ZR" to "R2",
    "−" to "Share", "+" to "Options",
    "LS" to "L3", "RS" to "R3",
    "Home" to "PS", "Camera" to "Touch",
    "D-Pad" to "Pad N/S/E/W",
)

@Composable
private fun Ds4NameTable() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.guideTableRowGap)) {
        DS4_BUTTON_NAMES.chunked(2).forEach { rowPairs ->
            Row {
                rowPairs.forEach { (joycon, ds4) ->
                    Ds4NameCell(joycon, ds4, Modifier.weight(1f))
                }
                if (rowPairs.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Ds4NameCell(joycon: String, ds4: String, modifier: Modifier = Modifier) {
    Row(modifier) {
        Text(
            joycon,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = Dimens.fontSizeSmall,
        )
        Text(
            " → $ds4",
            color = TextDim,
            fontSize = Dimens.fontSizeSmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun GuideStep(text: String) {
    Text(text, color = TextDim, fontSize = Dimens.fontSizeSmall)
}
