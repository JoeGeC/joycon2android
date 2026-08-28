package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/** Emulator picker paired with its one-shot config button, framed as a self-explaining group. */
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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmulatorDropdown(
                    options = emulators,
                    selectedId = selectedEmulator,
                    onSelect = onSelectEmulator,
                    modifier = Modifier.weight(1f),
                )
                DolphinSetupButton(phase, setupLabel, onSetUp, modifier = Modifier.weight(1f))
                if (onConfigureMapping != null) {
                    IconButton(onClick = onConfigureMapping) {
                        Icon(
                            Icons.Filled.Tune,
                            contentDescription = stringResource(R.string.emulator_auto_setup_configure_mapping),
                            tint = TextDim,
                        )
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoSetupInfoSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CardBg) {
        Column(
            Modifier
                .padding(horizontal = Dimens.cardPadding)
                .padding(bottom = Dimens.cardPadding)
                .navigationBarsPadding(),
        ) {
            Text(
                stringResource(R.string.emulator_auto_setup_title),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(Dimens.elementSpacing))
            Text(
                stringResource(R.string.emulator_auto_setup_info_body),
                color = TextDim,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
