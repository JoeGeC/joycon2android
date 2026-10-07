package com.joegec.joycon2android.connection.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.connection.presentation.R
import com.joegec.joycon2android.ui.theme.AppType
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.WarningText

// So that motion doing nothing in an emulator reads as the controller's limit, not a fault.
@Composable
internal fun NoGyroNote(modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.imu_no_gyro),
        modifier,
        color = WarningText,
        fontSize = Dimens.fontSizeLabel,
        style = noteTextStyle,
    )
}

private val noteTextStyle = AppType.telemetry.copy(
    lineHeight = Dimens.fontSizeLabel * 1.3f,
)
