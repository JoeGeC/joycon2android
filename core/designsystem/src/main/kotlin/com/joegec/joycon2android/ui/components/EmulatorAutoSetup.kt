package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
) {
    LabeledBorderBox(
        label = stringResource(R.string.emulator_auto_setup_title),
        borderColor = TextDim,
        labelBackground = CardBg,
        modifier = modifier,
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
            }
            if (phase == DolphinSetupPhase.FAILED) {
                Spacer(Modifier.height(Dimens.elementSpacing))
                DolphinSetupFailedText()
            }
        }
    }
}
