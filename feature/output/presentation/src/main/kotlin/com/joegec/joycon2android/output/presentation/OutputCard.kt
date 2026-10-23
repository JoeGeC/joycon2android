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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputMode
import com.joegec.joycon2android.ui.components.EmulatorAutoSetup
import com.joegec.joycon2android.ui.components.EmulatorOption
import com.joegec.joycon2android.ui.components.FeatureToggleCard
import com.joegec.joycon2android.ui.components.SegmentedChoice
import com.joegec.joycon2android.ui.components.StatusLight
import com.joegec.joycon2android.ui.theme.Dimens
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
    FeatureToggleCard(
        title = stringResource(R.string.output_title),
        subtitle = subtitle(state),
        checked = state.running,
        error = state.error,
        onToggle = actions.toggle,
        modifier = modifier,
    ) {
        Column(
            Modifier.padding(top = Dimens.elementSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        ) {
            AnimatedVisibility(
                visible = state.running,
                enter = fadeIn(Motion.easeOut()) + expandVertically(Motion.easeOut()),
                exit = fadeOut(Motion.easeOut()) + shrinkVertically(Motion.easeOut()),
            ) {
                DeviceLights(state.liveDevices)
            }
            SegmentedChoice(
                options = OutputMode.entries.map { it to it.label() },
                selected = state.selection.mode,
                onSelect = actions.setMode,
            )
            ModeContent(state, actions)
            status()
        }
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
            when (mode) {
                OutputMode.GAMEPAD, OutputMode.MOTION -> TargetSetup(mode, state, actions)
                OutputMode.MOUSE_KEYBOARD -> MappingButton(actions.openMapping)
            }
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

/** What the tab's output is doing: the gamepad, DSU's port and listeners, or mouse and keys. */
@Composable
private fun subtitle(state: OutputCardState): String {
    if (!state.running) return stringResource(R.string.output_subtitle_off)
    val mode = state.selection.mode
    val detail = if (mode == OutputMode.MOTION) state.motionDetail ?: mode.label() else mode.label()
    return stringResource(R.string.output_subtitle_on_for, detail)
}

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
private fun DeviceLights(live: Set<OutputDevice>) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        OutputDevice.entries.forEach { StatusLight(it.label(), lit = it in live) }
    }
}
