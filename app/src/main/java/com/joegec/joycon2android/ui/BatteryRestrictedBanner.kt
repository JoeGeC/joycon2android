package com.joegec.joycon2android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.R
import com.joegec.joycon2android.ui.components.WarningBox

@Composable
fun BatteryRestrictedBanner(
    visible: Boolean,
    onAllowBackgroundRunning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        WarningBox(
            text = stringResource(R.string.warning_battery_restricted),
            onClick = onAllowBackgroundRunning,
        )
    }
}
