package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.ui.theme.AppType
import com.joegec.joycon2android.ui.theme.CrosshairColor
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.StickBg
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun StickCard(
    x: Int,
    y: Int,
    pressed: Boolean,
    modifier: Modifier = Modifier,
    canvasSize: Dp = Dimens.stickCanvasSize,
) {
    val nx = (x - 2048f) / 2048f
    val ny = (y - 2048f) / 2048f
    val accent = LocalControllerAccent.current
    val ringColor = if (pressed) accent.color else accent.color.copy(alpha = 0.35f)

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        StickCanvas(nx, ny, ringColor, accent.color, pressed, canvasSize)
        Spacer(Modifier.height(6.dp))
        StickValues(x, y)
    }
}

@Composable
private fun StickCanvas(
    nx: Float,
    ny: Float,
    ringColor: Color,
    dotColor: Color,
    pressed: Boolean,
    canvasSize: Dp,
) {
    Canvas(Modifier.size(canvasSize)) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        val strokeWidth = if (pressed) Dimens.stickRingStrokePressed else Dimens.stickRingStroke

        drawCircle(color = StickBg, radius = r, center = c)
        drawCircle(color = ringColor, radius = r, center = c, style = Stroke(strokeWidth))
        drawLine(CrosshairColor, Offset(c.x - r, c.y), Offset(c.x + r, c.y), 1f)
        drawLine(CrosshairColor, Offset(c.x, c.y - r), Offset(c.x, c.y + r), 1f)

        val dot = Offset(c.x + nx * r, c.y - ny * r)
        drawCircle(color = dotColor, radius = Dimens.stickDotRadius, center = dot)
    }
}

@Composable
private fun StickValues(x: Int, y: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StickAxisValue("X", x)
        StickAxisValue("Y", y)
    }
}

@Composable
private fun StickAxisValue(axis: String, value: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            axis,
            color = TextDim.copy(alpha = 0.6f),
            fontSize = Dimens.fontSizeLabel,
            style = AppType.telemetry,
        )
        Text(
            "%4d".format(value),
            color = TextDim,
            fontSize = Dimens.fontSizeLabel,
            style = AppType.telemetry,
        )
    }
}
