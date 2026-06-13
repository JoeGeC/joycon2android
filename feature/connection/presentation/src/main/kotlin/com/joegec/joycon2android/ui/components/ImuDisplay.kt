package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.feature.connection.presentation.R
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun ImuDisplay(input: JoyconInput, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.imuSectionSpacing)) {
        SensorRow(stringResource(R.string.imu_accel), "X" to input.accelX, "Y" to input.accelY, "Z" to input.accelZ)
        SensorRow(stringResource(R.string.imu_gyro), "X" to input.gyroX, "Y" to input.gyroY, "Z" to input.gyroZ)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ImuLabel(stringResource(R.string.imu_packet))
            ImuValue(input.packetId.toString())
        }
    }
}

@Composable
private fun SensorRow(title: String, vararg axes: Pair<String, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.imuTitleGap)) {
        ImuLabel(title)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            axes.forEach { (axis, value) ->
                AxisValue(axis, value)
            }
        }
    }
}

@Composable
private fun AxisValue(axis: String, value: Int) {
    val style = imuTextStyle
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.imuAxisGap)) {
        Text(
            axis,
            color = TextDim.copy(alpha = 0.6f),
            fontSize = Dimens.fontSizeLabel,
            fontFamily = FontFamily.Monospace,
            style = style,
        )
        Text(
            "%+6d".format(value),
            color = TextDim,
            fontSize = Dimens.fontSizeLabel,
            fontFamily = FontFamily.Monospace,
            style = style,
        )
    }
}

@Composable
private fun ImuLabel(text: String) {
    Text(
        text,
        color = TextDim.copy(alpha = 0.7f),
        fontSize = Dimens.fontSizeLabel,
        fontWeight = FontWeight.Bold,
        letterSpacing = Dimens.fontSizeLabel * 0.1f,
        style = imuTextStyle,
    )
}

@Composable
private fun ImuValue(text: String) {
    Text(
        text,
        color = TextDim,
        fontSize = Dimens.fontSizeLabel,
        fontFamily = FontFamily.Monospace,
        style = imuTextStyle,
    )
}

private val imuTextStyle = TextStyle(
    lineHeight = Dimens.fontSizeLabel * 1.1f,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)
