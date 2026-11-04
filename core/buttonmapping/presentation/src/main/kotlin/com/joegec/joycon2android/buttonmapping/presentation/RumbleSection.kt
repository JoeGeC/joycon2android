package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.RUMBLE_PERCENT_RANGE
import com.joegec.joycon2android.buttonmapping.RUMBLE_PERCENT_STEP
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.InfoSheet
import com.joegec.joycon2android.ui.theme.TextDim

/** docs/virtual-gamepad.md#rumble */
@Composable
internal fun RumbleSection(console: Console, viaDsu: Boolean, percent: Int, open: Boolean, onSetPercent: (Int) -> Unit) {
    val throughPad = rumbleThroughPadInfo(console, viaDsu)
    var showInfo by rememberSaveable { mutableStateOf(false) }
    MappingSectionBox(MappingSection.RUMBLE, open, onInfoClick = throughPad?.let { { showInfo = true } }) {
        PercentSlider(
            title = stringResource(R.string.controller_mapping_rumble),
            description = stringResource(R.string.controller_mapping_rumble_description),
            percent = percent,
            range = RUMBLE_PERCENT_RANGE,
            step = RUMBLE_PERCENT_STEP,
            valueLabel = {
                if (it == 0) stringResource(R.string.controller_mapping_rumble_off)
                else stringResource(R.string.controller_mapping_percent_value, it)
            },
            onSetPercent = onSetPercent,
        )
    }
    if (showInfo && throughPad != null) {
        InfoSheet(stringResource(R.string.rumble_through_pad_title), onDismiss = { showInfo = false }) {
            Text(stringResource(throughPad), color = TextDim, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// DSU carries no rumble, so on the Motion tab it comes through the virtual gamepad.
private fun rumbleThroughPadInfo(console: Console, viaDsu: Boolean): Int? = when {
    console == Console.WIIMOTE_NUNCHUK -> R.string.rumble_through_pad_wii
    console == Console.SWITCH_PRO && viaDsu -> R.string.rumble_through_pad_eden
    else -> null
}
