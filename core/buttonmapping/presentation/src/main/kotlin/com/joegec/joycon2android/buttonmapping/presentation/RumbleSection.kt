package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.RUMBLE_PERCENT_RANGE
import com.joegec.joycon2android.buttonmapping.RUMBLE_PERCENT_STEP
import com.joegec.joycon2android.core.buttonmapping.presentation.R

/** docs/virtual-gamepad.md#rumble */
@Composable
internal fun RumbleSection(percent: Int, open: Boolean, onSetPercent: (Int) -> Unit) {
    MappingSectionBox(MappingSection.RUMBLE, open) {
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
}
