package com.joegec.joycon2android.dsu.presentation
import com.joegec.joycon2android.ui.components.EmulatorAutoSetup
import com.joegec.joycon2android.ui.components.FeatureToggleCard
import com.joegec.joycon2android.ui.components.WarningBox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier,
) {
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
                slotLimitText(state.coverage)?.let { WarningBox(it) }
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
