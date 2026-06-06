package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun ImuDisplay(input: JoyconInput, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Column {
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
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            axis,
            color = TextDim.copy(alpha = 0.6f),
            fontSize = Dimens.fontSizeLabel,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            "%+6d".format(value),
            color = TextDim,
            fontSize = Dimens.fontSizeLabel,
            fontFamily = FontFamily.Monospace,
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
    )
}

@Composable
private fun ImuValue(text: String) {
    Text(
        text,
        color = TextDim,
        fontSize = Dimens.fontSizeLabel,
        fontFamily = FontFamily.Monospace,
    )
}
