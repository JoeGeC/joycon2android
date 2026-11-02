package com.joegec.joycon2android.output.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.OutputMode
import com.joegec.joycon2android.ui.components.EmulatorAutoSetup
import com.joegec.joycon2android.ui.components.EmulatorOption
import com.joegec.joycon2android.ui.components.SegmentedChoice
import com.joegec.joycon2android.ui.components.SettingWarning
import com.joegec.joycon2android.ui.components.ToggleRow
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.ErrorText
import com.joegec.joycon2android.ui.theme.Motion
import com.joegec.joycon2android.ui.theme.TextDim

/** The one place outputs are chosen: docs/DESIGN.md#output-card. [status] carries each device's own news. */
@Composable
fun OutputCard(
    state: OutputCardState,
    actions: OutputCardActions,
    modifier: Modifier = Modifier,
    status: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(Dimens.buttonCorner))
            .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
    ) {
        SegmentedChoice(
            options = OutputMode.entries.map { it to it.label() },
            selected = state.selection.mode,
            onSelect = actions.setMode,
            lit = OutputMode.entries.filterTo(mutableSetOf(), state::isLit),
        )
        ModeContent(state, actions)
        status()
    }
}

/** Cross-fades between tabs while the card eases to the new content's height. */
@Composable
private fun ModeContent(state: OutputCardState, actions: OutputCardActions) {
    AnimatedContent(
        targetState = state.selection.mode,
        transitionSpec = {
            (fadeIn(Motion.easeOut()) togetherWith fadeOut(Motion.easeOut()))
                .using(SizeTransform(clip = false) { _, _ -> Motion.easeOut() })
        },
        label = "mode",
    ) { mode ->
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
            ToggleRow(
                status = outputStatus(mode, state),
                checked = mode in state.running,
                onToggle = { actions.toggle(mode, it) },
                lit = state.isLit(mode) || mode in state.running,
            )
            AnimatedVisibility(
                visible = mode in state.running,
                enter = fadeIn(Motion.easeOut()) + expandVertically(Motion.easeOut()),
                exit = fadeOut(Motion.easeOut()) + shrinkVertically(Motion.easeOut()),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
                    when (mode) {
                        OutputMode.GAMEPAD, OutputMode.MOTION -> {
                            TargetSetup(mode, state, actions)
                            if (state.setupsCollide) SettingWarning(stringResource(R.string.output_setups_collide))
                        }
                        OutputMode.MOUSE_KEYBOARD -> {
                            MappingButton(actions.openMapping)
                            if (state.noPointerController) SettingWarning(stringResource(R.string.output_no_pointer))
                        }
                    }
                }
            }
            ErrorLine(state.errors[mode]?.takeIf { mode in state.running } ?: shizukuError(state, mode))
        }
    }
}

/** [mode]'s own emulators, kept per tab, so a tab can draw them while it fades out. */
@Composable
private fun TargetSetup(mode: OutputMode, state: OutputCardState, actions: OutputCardActions) {
    val targets = state.emulatorsFor(mode)
    val selectedId = state.selectedEmulatorFor(mode)
    val setsUp = targets.firstOrNull { it.id == selectedId }?.setsUp == true
    EmulatorAutoSetup(
        emulators = targets.map { EmulatorOption(it.id, it.label()) },
        selectedEmulator = selectedId.orEmpty(),
        onSelectEmulator = actions.selectEmulator,
        phase = state.setupPhase,
        setupLabel = stringResource(R.string.output_set_up),
        // Other sets nothing up, leaving only its mapping, labelled to fill the row.
        onSetUp = if (setsUp) actions.setUp else null,
        onConfigureMapping = actions.openMapping,
        mappingLabel = if (wide() || !setsUp) stringResource(R.string.output_mapping) else null,
    )
}

@Composable
private fun wide() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

@Composable
private fun MappingButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.buttonCorner),
    ) {
        Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = TextDim, modifier = Modifier.size(Dimens.iconSizeSmall))
        Text(stringResource(R.string.output_mapping), color = TextDim, modifier = Modifier.padding(start = Dimens.elementSpacing))
    }
}

@Composable
private fun ErrorLine(error: String?) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn(Motion.easeOut()) + expandVertically(Motion.easeOut()),
        exit = fadeOut(Motion.easeOut()) + shrinkVertically(Motion.easeOut()),
    ) {
        Text(error.orEmpty(), color = ErrorText, style = MaterialTheme.typography.bodySmall)
    }
}
