package com.joegec.joycon2android.dsu.presentation
import com.joegec.joycon2android.ui.components.EmulatorAutoSetup
import com.joegec.joycon2android.ui.components.EmulatorOption
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.dsu.DsuCoverage
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.ui.theme.Dimens

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
        contentPadding = cardPadding(state.enabled),
    ) {
        AnimatedVisibility(
            visible = state.enabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                Modifier.padding(top = Dimens.elementSpacing),
                verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
            ) {
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
                }
                SettingsRow(
                    title = stringResource(R.string.dsu_motion_settings_title),
                    onClick = { showMotionSettings = true },
                )
                slotLimitText(state.coverage)?.let { WarningBox(it) }
            }
        }
    }
}

// The Motion settings row's 48dp touch target already leaves space below it.
private fun cardPadding(enabled: Boolean) = PaddingValues(
    start = Dimens.cardPadding,
    top = Dimens.cardPadding,
    end = Dimens.cardPadding,
    bottom = if (enabled) Dimens.elementSpacing else Dimens.cardPadding,
)

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
