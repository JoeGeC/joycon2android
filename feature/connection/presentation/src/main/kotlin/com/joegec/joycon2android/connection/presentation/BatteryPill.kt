package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.connection.presentation.R
import com.joegec.joycon2android.model.BatteryGauge
import com.joegec.joycon2android.ui.theme.AccentDim
import com.joegec.joycon2android.ui.theme.AppType
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.batteryColor

@Composable
internal fun BatteryPill(volts: Float, modifier: Modifier = Modifier) {
    BatteryReadout(
        volts = volts,
        modifier = modifier
            .background(AccentDim, RoundedCornerShape(Dimens.pillCorner))
            .padding(horizontal = Dimens.pillPaddingHorizontal, vertical = Dimens.pillPaddingVertical),
    )
}

@Composable
internal fun BatteryReadout(volts: Float, modifier: Modifier = Modifier) {
    val percent = BatteryGauge.percentFromVolts(volts)
    val color = batteryColor(percent)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        BatteryIcon(percent, color)
        Text(
            stringResource(R.string.battery_percent_format, percent),
            color = color,
            style = AppType.telemetry,
            fontSize = Dimens.fontSizeBattery,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = Dimens.batteryIconTextGap),
        )
    }
}

@Composable
internal fun BatteryGlyph(volts: Float, modifier: Modifier = Modifier) {
    val percent = BatteryGauge.percentFromVolts(volts)
    BatteryIcon(percent, batteryColor(percent), modifier)
}

@Composable
private fun BatteryIcon(percent: Int, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(Dimens.batteryIconWidth, Dimens.batteryIconHeight)) {
        val strokeWidth = Dimens.batteryIconStroke.toPx()
        val capWidth = Dimens.batteryIconCapWidth.toPx()
        val corner = CornerRadius(Dimens.batteryIconCorner.toPx())
        val bodyWidth = size.width - capWidth
        drawRoundRect(
            color = color,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(bodyWidth - strokeWidth, size.height - strokeWidth),
            cornerRadius = corner,
            style = Stroke(strokeWidth),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(bodyWidth, size.height * 0.3f),
            size = Size(capWidth, size.height * 0.4f),
            cornerRadius = corner,
        )
        val fillInset = strokeWidth * 1.5f
        val fillWidth = (bodyWidth - fillInset * 2) * percent / 100f
        if (fillWidth > 0f) {
            drawRoundRect(
                color = color,
                topLeft = Offset(fillInset, fillInset),
                size = Size(fillWidth, size.height - fillInset * 2),
                cornerRadius = CornerRadius(corner.x / 2),
            )
        }
    }
}
