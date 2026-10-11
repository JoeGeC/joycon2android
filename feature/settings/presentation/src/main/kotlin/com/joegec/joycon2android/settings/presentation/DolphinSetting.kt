package com.joegec.joycon2android.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun DolphinSetting(
    calibrateSticks: Boolean,
    onCalibrateSticksToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        Text(
            stringResource(R.string.settings_dolphin_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        SettingSwitch(
            title = stringResource(R.string.settings_dolphin_calibrate_sticks_title),
            description = stringResource(R.string.settings_dolphin_calibrate_sticks_description),
            checked = calibrateSticks,
            onCheckedChange = onCalibrateSticksToggle,
        )
    }
}
