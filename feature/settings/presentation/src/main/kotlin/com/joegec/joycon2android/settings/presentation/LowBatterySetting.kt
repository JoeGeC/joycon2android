package com.joegec.joycon2android.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.settings.LowBatteryAlert
import com.joegec.joycon2android.ui.components.PercentRangeSlider
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun LowBatterySetting(
    alert: LowBatteryAlert,
    notificationsAllowed: Boolean,
    actions: LowBatteryActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing)) {
        SettingSwitch(
            title = stringResource(R.string.settings_low_battery_notification_title),
            description = null,
            checked = alert.notify && notificationsAllowed,
            onCheckedChange = actions.toggleNotification,
            titleStyle = MaterialTheme.typography.titleMedium,
        )
        SettingSwitch(
            title = stringResource(R.string.settings_low_battery_beep_title),
            description = null,
            checked = alert.vibrate,
            onCheckedChange = actions.toggleVibration,
            titleStyle = MaterialTheme.typography.titleMedium,
        )
        PercentRangeSlider(
            title = stringResource(R.string.settings_low_battery_warn_at_title),
            low = alert.finalWarningPercent,
            high = alert.firstWarningPercent,
            range = LowBatteryAlert.MIN_WARNING_PERCENT..LowBatteryAlert.MAX_WARNING_PERCENT,
            step = LowBatteryAlert.WARNING_STEP_PERCENT,
            valueLabel = { low, high -> warnAtLabel(low, high) },
            onSetPercents = { low, high -> actions.setWarnings(high, low) },
        )
    }
}

@Composable
private fun warnAtLabel(low: Int, high: Int): String =
    if (low == high) stringResource(R.string.settings_low_battery_warn_at_one, low)
    else stringResource(R.string.settings_low_battery_warn_at_two, high, low)
