package com.joegec.joycon2android.output.presentation

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
import androidx.compose.foundation.layout.Row
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
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.DeviceOverride
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputMode
import com.joegec.joycon2android.ui.components.EmulatorAutoSetup
import com.joegec.joycon2android.ui.components.EmulatorOption
import com.joegec.joycon2android.ui.components.ExpandableInfoSection
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
    val mode = state.selection.mode
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
            SegmentedChoice(
                options = OutputMode.entries.map { it to it.label() },
                selected = mode,
                onSelect = actions.setMode,
            )
            ModeContent(mode, state, actions)
            AnimatedVisibility(
                visible = state.running,
                enter = fadeIn(Motion.easeOut()) + expandVertically(Motion.easeOut()),
                exit = fadeOut(Motion.easeOut()) + shrinkVertically(Motion.easeOut()),
            ) {
                DeviceLights(state.liveDevices)
            }
            status()
            Advanced(state, actions.setOverride)
        }
    }
}

/** Cross-fades between modes while the card eases to the new content's height. */
@Composable
private fun ModeContent(mode: OutputMode, state: OutputCardState, actions: OutputCardActions) {
    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            (fadeIn(Motion.easeOut()) togetherWith fadeOut(Motion.easeOut()))
                .using(SizeTransform(clip = false) { _, _ -> Motion.easeOut() })
        },
        label = "mode",
    ) { shown ->
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
            when (shown) {
                OutputMode.CONTROLLER -> TargetSetup(state, actions)
                OutputMode.MOUSE_KEYBOARD -> {
                    Text(stringResource(R.string.output_mode_mouse_keyboard_description), color = TextDim, style = MaterialTheme.typography.bodySmall)
                    MappingButton(actions.openMapping)
                }
            }
        }
    }
}

@Composable
private fun TargetSetup(state: OutputCardState, actions: OutputCardActions) {
    val selected = state.emulators.firstOrNull { it.id == state.selectedEmulator }
    val automatic = selected?.packageName != null
    EmulatorAutoSetup(
        emulators = state.emulators.map { EmulatorOption(it.id, it.label(), it.useNote()) },
        selectedEmulator = state.selectedEmulator.orEmpty(),
        onSelectEmulator = actions.selectEmulator,
        phase = state.setupPhase,
        setupLabel = stringResource(if (automatic) R.string.output_set_up else R.string.output_how_to_connect),
        onSetUp = actions.setUp,
        onConfigureMapping = actions.openMapping,
        mappingLabel = if (wide()) stringResource(R.string.output_mapping) else null,
        automatic = automatic,
    )
}

@Composable
private fun wide() = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

@Composable
private fun subtitle(state: OutputCardState): String {
    if (!state.running) return stringResource(R.string.output_subtitle_off)
    val playing = when (state.selection.mode) {
        OutputMode.CONTROLLER -> state.emulators.firstOrNull { it.id == state.selectedEmulator }?.label()
        OutputMode.MOUSE_KEYBOARD -> state.selection.mode.label()
    }
    return playing?.let { stringResource(R.string.output_subtitle_on_for, it) } ?: stringResource(R.string.output_subtitle_on)
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

@Composable
private fun Advanced(state: OutputCardState, setOverride: (OutputDevice, DeviceOverride) -> Unit) {
    ExpandableInfoSection(title = stringResource(R.string.output_advanced)) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
            Text(stringResource(R.string.output_advanced_note), color = TextDim, style = MaterialTheme.typography.bodySmall)
            OutputDevice.entries.forEach { device ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(device.label(), color = TextDim, modifier = Modifier.weight(1f))
                    SegmentedChoice(
                        options = DeviceOverride.entries.map { it to it.label() },
                        selected = state.selection.override(device),
                        onSelect = { setOverride(device, it) },
                        modifier = Modifier.weight(2f),
                    )
                }
            }
        }
    }
}
