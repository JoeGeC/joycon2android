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
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.ui.components.EmulatorAutoSetup
import com.joegec.joycon2android.ui.components.InfoBox
import com.joegec.joycon2android.ui.components.SettingWarning
import com.joegec.joycon2android.ui.components.StatusLine
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.ErrorText
import com.joegec.joycon2android.ui.theme.Motion
import com.joegec.joycon2android.ui.theme.TextDim

/** The one place outputs are chosen, by what's being played: docs/DESIGN.md#output-card. [status] carries each device's own news. */
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
        PlayingPicker(state, actions.select)
        StatusLine(
            outputStatus(state),
            lit = state.liveDevices.isNotEmpty(),
            modifier = Modifier.padding(vertical = Dimens.statusLinePadding),
        )
        PlayingContent(state, actions)
        ErrorLine(state.error ?: shizukuError(state))
        status()
    }
}

/** Cross-fades between games while the card eases to the new content's height. */
@Composable
private fun PlayingContent(state: OutputCardState, actions: OutputCardActions) {
    AnimatedContent(
        targetState = state.playing,
        transitionSpec = {
            (fadeIn(Motion.easeOut()) togetherWith fadeOut(Motion.easeOut()))
                .using(SizeTransform(clip = false) { _, _ -> Motion.easeOut() })
        },
        label = "playing",
    ) { playing ->
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
            when {
                playing.emulator != null && state.targets.isNotEmpty() -> EmulatorSetup(state, actions)
                playing.emulator != null -> {
                    InfoBox(stringResource(R.string.playing_not_installed, playing.label()))
                    MappingButton(actions.openMapping)
                }
                playing == Playing.OTHER_EMULATOR -> {
                    InfoBox(stringResource(R.string.playing_other_emulator_steps, state.dsuAddress))
                    MappingButton(actions.openMapping)
                }
                else -> MappingButton(actions.openMapping)
            }
            if (playing == Playing.PC && state.noPointerController) SettingWarning(stringResource(R.string.output_no_pointer))
        }
    }
}

@Composable
private fun EmulatorSetup(state: OutputCardState, actions: OutputCardActions) {
    EmulatorAutoSetup(
        phase = state.setupPhase,
        setupLabel = stringResource(R.string.output_set_up),
        onSetUp = actions.setUp,
        onConfigureMapping = actions.openMapping,
        mappingLabel = if (wide()) stringResource(R.string.output_mapping) else null,
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
