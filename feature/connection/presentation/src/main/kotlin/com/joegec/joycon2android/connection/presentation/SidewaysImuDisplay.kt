package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.joegec.joycon2android.connection.presentation.R
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.ui.theme.AppType
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextBright
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun SidewaysImuDisplay(input: JoyconInput, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.imuRowSpacing)) {
        SensorRow(input)
        PacketRow(input)
    }
}

@Composable
private fun SensorRow(input: JoyconInput) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.cardPadding),
    ) {
        SensorColumn(stringResource(R.string.imu_accel), input.accelX, input.accelY, input.accelZ, Modifier.weight(1f))
        SensorColumn(stringResource(R.string.imu_gyro), input.gyroX, input.gyroY, input.gyroZ, Modifier.weight(1f))
    }
}

@Composable
private fun PacketRow(input: JoyconInput) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.cardPadding),
    ) {
        Spacer(Modifier.weight(1f))
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
            ImuText(stringResource(R.string.imu_packet), bold = true)
            ImuText(input.packetId.toString())
        }
    }
}

@Composable
private fun SensorColumn(title: String, x: Int, y: Int, z: Int, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.imuTitleGap)) {
        ImuText(title, bold = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AxisValue("X", x)
            AxisValue("Y", y)
            AxisValue("Z", z)
        }
    }
}

@Composable
private fun AxisValue(axis: String, value: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.imuAxisGap)) {
        ImuText(axis, dimmed = true)
        ImuText("%+6d".format(value))
    }
}

@Composable
private fun ImuText(text: String, bold: Boolean = false, dimmed: Boolean = false) {
    Text(
        text,
        color = if (bold || dimmed) TextDim else TextBright,
        fontSize = Dimens.fontSizeLabel,
        fontWeight = if (bold) FontWeight.Bold else null,
        fontFamily = if (bold) FontFamily.Default else null,
        letterSpacing = if (bold) Dimens.fontSizeLabel * 0.1f else TextUnit.Unspecified,
        style = tightTextStyle,
    )
}

// Telemetry (mono, tabular figures, no font padding) pulled tight so the IMU grid stays compact;
// bold section labels override back to the default family.
private val tightTextStyle = AppType.telemetry.copy(
    lineHeight = Dimens.fontSizeLabel * 1.1f,
)
