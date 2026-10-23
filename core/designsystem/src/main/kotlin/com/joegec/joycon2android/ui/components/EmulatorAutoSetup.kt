package com.joegec.joycon2android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.Motion
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun EmulatorAutoSetup(
    emulators: List<EmulatorOption>,
    selectedEmulator: String,
    onSelectEmulator: (String) -> Unit,
    phase: DolphinSetupPhase,
    setupLabel: String,
    /** Null for a choice with nothing to set up, leaving the mapping button to fill the row. */
    onSetUp: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onConfigureMapping: (() -> Unit)? = null,
    /** Shown beside the mapping icon where there's room; icon only when null. */
    mappingLabel: String? = null,
) {
    var showInfo by remember { mutableStateOf(false) }

    LabeledBorderBox(
        label = stringResource(R.string.emulator_auto_setup_title),
        borderColor = TextDim,
        labelBackground = CardBg,
        modifier = modifier,
        onInfoClick = { showInfo = true },
    ) {
        Column {
            EmulatorDropdown(
                options = emulators,
                selectedId = selectedEmulator,
                onSelect = onSelectEmulator,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Dimens.emulatorSetupRowGap))
            SetUpRow(phase, setupLabel, onSetUp, onConfigureMapping, mappingLabel)
            val tips = LocalTips.current
            AnimatedVisibility(
                // A tip for when auto setup misbehaves, so not where there's nothing to set up.
                visible = onConfigureMapping != null && onSetUp != null && tips.visible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                HintBubble(
                    stringResource(R.string.emulator_auto_setup_mapping_hint),
                    pointerCenterFromEnd = Dimens.emulatorSetupButtonHeight / 2,
                    modifier = Modifier.padding(top = Dimens.emulatorSetupRowGap),
                    onClick = tips.onTap,
                )
            }
            if (phase.isFailure) {
                Spacer(Modifier.height(Dimens.elementSpacing))
                DolphinSetupMessage(phase)
            }
        }
    }

    if (showInfo) {
        AutoSetupInfoSheet(onDismiss = { showInfo = false })
    }
}

/** Set up and the mapping button, the mapping button easing to the full row as Set up leaves. */
@Composable
private fun SetUpRow(
    phase: DolphinSetupPhase,
    setupLabel: String,
    onSetUp: (() -> Unit)?,
    onConfigureMapping: (() -> Unit)?,
    mappingLabel: String?,
) {
    val split by animateFloatAsState(if (onSetUp != null) 1f else 0f, Motion.easeOut(), label = "setUp")
    // Kept so Set up can fade out after its choice has gone.
    var lastSetUp by remember { mutableStateOf(onSetUp) }
    if (onSetUp != null) lastSetUp = onSetUp
    if (onConfigureMapping == null) {
        lastSetUp?.let { DolphinSetupButton(phase, setupLabel, it, modifier = Modifier.fillMaxWidth()) }
        return
    }
    SplitRow(
        split = split,
        gap = Dimens.elementSpacing,
        start = {
            lastSetUp?.let {
                DolphinSetupButton(phase, setupLabel, it, modifier = Modifier.alpha(split).clipToBounds())
            }
        },
        end = { MappingButton(onConfigureMapping, mappingLabel) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MappingButton(onClick: () -> Unit, label: String?) {
    var lastLabel by remember { mutableStateOf(label) }
    if (label != null) lastLabel = label
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .height(Dimens.emulatorSetupButtonHeight)
            .widthIn(min = Dimens.emulatorSetupButtonHeight),
        shape = RoundedCornerShape(Dimens.buttonCorner),
        contentPadding = PaddingValues(0.dp),
    ) {
        // A fixed box, so the icon alone keeps its padding at its natural width.
        Box(
            Modifier.size(width = Dimens.emulatorMappingIconWidth, height = Dimens.emulatorSetupButtonHeight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = if (label == null) stringResource(R.string.emulator_auto_setup_configure_mapping) else null,
                tint = TextDim,
            )
        }
        AnimatedVisibility(
            visible = label != null,
            enter = fadeIn(Motion.easeOut()) + expandHorizontally(Motion.easeOut()),
            exit = fadeOut(Motion.easeOut()) + shrinkHorizontally(Motion.easeOut()),
        ) {
            Text(
                lastLabel.orEmpty(),
                color = TextDim,
                maxLines = 1,
                modifier = Modifier.padding(end = Dimens.emulatorPickerPadding),
            )
        }
    }
}

@Composable
private fun AutoSetupInfoSheet(onDismiss: () -> Unit) {
    InfoSheet(title = stringResource(R.string.emulator_auto_setup_title), onDismiss = onDismiss) {
        Text(
            stringResource(R.string.emulator_auto_setup_info_body),
            color = TextDim,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
