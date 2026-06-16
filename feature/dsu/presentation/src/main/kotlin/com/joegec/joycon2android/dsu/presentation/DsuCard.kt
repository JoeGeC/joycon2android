package com.joegec.joycon2android.dsu.presentation
import com.joegec.joycon2android.ui.components.CopyableCode
import com.joegec.joycon2android.ui.components.DolphinSetupButton
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
import com.joegec.joycon2android.ui.components.FeatureToggleCard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.dsu.presentation.R
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun DsuCard(
    state: DsuCardState,
    onToggle: (Boolean) -> Unit,
    onConfigureDolphin: () -> Unit,
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
                EmulatorGuides(state, onConfigureDolphin)
            }
        }
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
        if (address != null) {
            CopyableCode(address)
        }
    }
}

// Per-emulator quirks live behind expandable sections; add new emulators here
@Composable
private fun EmulatorGuides(state: DsuCardState, onConfigureDolphin: () -> Unit) {
    ExpandableInfoSection(stringResource(R.string.dsu_dolphin_guide_title)) {
        DolphinGuide(state, onConfigureDolphin)
    }
    ExpandableInfoSection(stringResource(R.string.dsu_mapping_trouble_title)) {
        MappingTroubleshooting()
    }
}

@Composable
private fun DolphinGuide(state: DsuCardState, onConfigureDolphin: () -> Unit) {
    Column {
        if (state.dolphinInstalled && state.dolphinAutoConfigAvailable) {
            DolphinSetupButton(
                state.dolphinPhase,
                stringResource(R.string.dsu_dolphin_auto_setup),
                onConfigureDolphin,
            )
            Spacer(Modifier.height(Dimens.elementSpacing))
            GuideStep(stringResource(R.string.dsu_dolphin_manual_label))
            Spacer(Modifier.height(Dimens.elementSpacing))
        }
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
