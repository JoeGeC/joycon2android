package com.joegec.joycon2android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun EmulatorAutoSetup(
    emulators: List<EmulatorOption>,
    selectedEmulator: String,
    onSelectEmulator: (String) -> Unit,
    phase: DolphinSetupPhase,
    setupLabel: String,
    onSetUp: () -> Unit,
    modifier: Modifier = Modifier,
    onConfigureMapping: (() -> Unit)? = null,
    /** Shown beside the mapping icon where there's room; icon only when null. */
    mappingLabel: String? = null,
    /** False for a target set up by hand, whose [onSetUp] opens a guide of its own. */
    automatic: Boolean = true,
) {
    var showInfo by remember { mutableStateOf(false) }

    LabeledBorderBox(
        label = stringResource(if (automatic) R.string.emulator_auto_setup_title else R.string.emulator_manual_setup_title),
        borderColor = TextDim,
        labelBackground = CardBg,
        modifier = modifier,
        onInfoClick = if (automatic) ({ showInfo = true }) else null,
    ) {
        Column {
            EmulatorDropdown(
                options = emulators,
                selectedId = selectedEmulator,
                onSelect = onSelectEmulator,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Dimens.emulatorSetupRowGap))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DolphinSetupButton(phase, setupLabel, onSetUp, modifier = Modifier.weight(1f))
                if (onConfigureMapping != null) {
                    MappingButton(onConfigureMapping, mappingLabel)
                }
            }
            val tips = LocalTips.current
            AnimatedVisibility(
                visible = onConfigureMapping != null && tips.visible,
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

@Composable
private fun MappingButton(onClick: () -> Unit, label: String?, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = if (label == null) modifier.size(Dimens.emulatorSetupButtonHeight)
        else modifier.height(Dimens.emulatorSetupButtonHeight),
        shape = RoundedCornerShape(Dimens.buttonCorner),
        contentPadding = if (label == null) PaddingValues(0.dp)
        else PaddingValues(horizontal = Dimens.emulatorPickerPadding),
    ) {
        Icon(
            Icons.Filled.SportsEsports,
            contentDescription = if (label == null) stringResource(R.string.emulator_auto_setup_configure_mapping) else null,
            tint = TextDim,
        )
        if (label != null) {
            Text(label, color = TextDim, modifier = Modifier.padding(start = Dimens.elementSpacing))
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
