package com.joegec.joycon2android.dsu.presentation
import com.joegec.joycon2android.ui.components.CopyableCode
import com.joegec.joycon2android.ui.components.EmulatorAutoSetup
import com.joegec.joycon2android.ui.components.EmulatorOption
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
import com.joegec.joycon2android.ui.components.FeatureToggleCard
import com.joegec.joycon2android.ui.components.SettingsRow
import com.joegec.joycon2android.ui.components.WarningBox

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.dsu.DsuCoverage
import com.joegec.joycon2android.dsu.presentation.R
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.ui.theme.AppType
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun DsuCard(
    state: DsuCardState,
    onToggle: (Boolean) -> Unit,
    onSelectEmulator: (String) -> Unit,
    onSetUp: () -> Unit,
    onConfigureMapping: () -> Unit,
    onFastMotionToggle: (Boolean) -> Unit,
    onBlockDeviceMotionToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMotionSettings by rememberSaveable { mutableStateOf(false) }
    if (showMotionSettings) {
        DsuMotionSettingsDialog(
            settings = state.motionSettings,
            deviceMotionBlockAvailable = state.deviceMotionBlockAvailable,
            onFastMotionToggle = onFastMotionToggle,
            onBlockDeviceMotionToggle = onBlockDeviceMotionToggle,
            onDismiss = { showMotionSettings = false },
        )
    }
    FeatureToggleCard(
        title = stringResource(R.string.dsu_title),
        subtitle = subtitleFor(state),
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
                if (state.emulators.isNotEmpty()) {
                    EmulatorAutoSetup(
                        emulators = state.emulators,
                        selectedEmulator = state.selectedEmulator,
                        onSelectEmulator = onSelectEmulator,
                        phase = state.setupPhase,
                        setupLabel = stringResource(R.string.dsu_auto_setup),
                        onSetUp = onSetUp,
                        onConfigureMapping = onConfigureMapping,
                    )
                    Spacer(Modifier.height(Dimens.elementSpacing))
                }
                SettingsRow(
                    title = stringResource(R.string.dsu_motion_settings_title),
                    onClick = { showMotionSettings = true },
                )
                Spacer(Modifier.height(Dimens.elementSpacing))
                slotLimitText(state.coverage)?.let { warning ->
                    WarningBox(warning)
                    Spacer(Modifier.height(Dimens.elementSpacing))
                }
                ExpandableInfoSection(stringResource(R.string.dsu_manual_setup_title)) {
                    ManualEmulatorSetup(state.address)
                }
                ExpandableInfoSection(stringResource(R.string.dsu_mapping_trouble_title)) {
                    MappingTroubleshooting()
                }
            }
        }
    }
}

@Composable
private fun subtitleFor(state: DsuCardState): String = when {
    !state.enabled -> stringResource(R.string.dsu_subtitle_off)
    state.clientCount == 0 -> stringResource(R.string.dsu_subtitle_waiting, DsuConfig.PORT)
    else -> pluralStringResource(
        R.plurals.dsu_subtitle_on,
        state.clientCount,
        DsuConfig.PORT,
        state.clientCount,
    )
}

@Composable
private fun slotLimitText(coverage: DsuCoverage): String? {
    if (coverage.allStreamed) return null
    val lines = mutableListOf(stringResource(R.string.dsu_slot_limit_lead))
    if (coverage.unservedPlayers.isNotEmpty()) {
        lines += stringResource(R.string.dsu_slot_limit_players, playerLabels(coverage.unservedPlayers))
    }
    if (coverage.unservedSecondHands.isNotEmpty()) {
        lines += stringResource(R.string.dsu_slot_limit_second_hands, playerLabels(coverage.unservedSecondHands))
    }
    return lines.joinToString("\n")
}

@Composable
private fun playerLabels(players: List<PlayerNumber>): String {
    val template = stringResource(R.string.player_label)
    return players.joinToString { template.format(it.index) }
}

@Composable
private fun ManualEmulatorSetup(address: String?) {
    Column {
        GuideStep(stringResource(R.string.dsu_setup_body))
        Spacer(Modifier.height(Dimens.elementSpacing))
        if (address != null) {
            CopyableCode(address)
            Spacer(Modifier.height(Dimens.elementSpacing))
        }
        ExpandableInfoSection(stringResource(R.string.dsu_dolphin_emulator_name)) {
            DolphinManualSteps()
        }
        ExpandableInfoSection(stringResource(R.string.dsu_eden_emulator_name)) {
            EdenManualSteps()
        }
    }
}

@Composable
private fun EdenManualSteps() {
    Column {
        GuideStep(stringResource(R.string.dsu_eden_servers))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_eden_motion))
    }
}

@Composable
private fun DolphinManualSteps() {
    Column {
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
        GuideStep(stringResource(R.string.dsu_mapping_second_hand))
        Spacer(Modifier.height(Dimens.elementSpacing))
        GuideStep(stringResource(R.string.dsu_mapping_trouble_gamepad))
    }
}

// DSU carries exactly the DS4 button set — these are protocol input names, not UI copy.
// SL/SR/Chat have no DSU slot (see dsu_mapping_missing); Capture rides the touchpad click.
private val DS4_BUTTON_NAMES = listOf(
    "A" to "Circle", "B" to "Cross",
    "X" to "Triangle", "Y" to "Square",
    "L" to "L1", "R" to "R1",
    "ZL" to "L2", "ZR" to "R2",
    "−" to "Share", "+" to "Options",
    "LS" to "L3", "RS" to "R3",
    "Home" to "PS", "Capture" to "Touch",
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
            style = AppType.telemetry,
            fontSize = Dimens.fontSizeSmall,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
        )
        Text(
            " → $ds4",
            color = TextDim,
            style = AppType.telemetry,
            fontSize = Dimens.fontSizeSmall,
        )
    }
}

@Composable
private fun GuideStep(text: String) {
    Text(text, color = TextDim, style = MaterialTheme.typography.bodySmall)
}
