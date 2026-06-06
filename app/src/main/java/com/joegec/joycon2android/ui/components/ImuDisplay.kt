package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun ImuDisplay(input: JoyconInput, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SensorRow("Accel", "X" to input.accelX, "Y" to input.accelY, "Z" to input.accelZ)
        SensorRow("Gyro", "X" to input.gyroX, "Y" to input.gyroY, "Z" to input.gyroZ)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Label("Packet")
            Value(input.packetId.toString())
        }
    }
}

@Composable
private fun SensorRow(title: String, vararg axes: Pair<String, Int>) {
    Column {
        Label(title)
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
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            "%+6d".format(value),
            color = TextDim,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        color = TextDim.copy(alpha = 0.7f),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun Value(text: String) {
    Text(
        text,
        color = TextDim,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
    )
}
