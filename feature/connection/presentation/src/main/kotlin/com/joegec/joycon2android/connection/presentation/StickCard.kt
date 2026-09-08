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
import com.joegec.joycon2android.ui.theme.AppType
import com.joegec.joycon2android.ui.theme.CrosshairColor
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.StickBg
import com.joegec.joycon2android.ui.theme.TextBright
import com.joegec.joycon2android.ui.theme.TextDim
import kotlin.math.hypot

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
    val ringColor = if (pressed) accent.color else accent.color.copy(alpha = Dimens.stickIdleRingAlpha)

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        StickCanvas(nx, ny, ringColor, accent.color, pressed, canvasSize)
        Spacer(Modifier.height(Dimens.stickValueGap))
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
        drawLine(CrosshairColor, Offset(c.x - r, c.y), Offset(c.x + r, c.y), Dimens.crosshairStroke)
        drawLine(CrosshairColor, Offset(c.x, c.y - r), Offset(c.x, c.y + r), Dimens.crosshairStroke)

        val dot = dotPosition(c, nx, ny, r - Dimens.stickDotRadius)
        drawCircle(color = dotColor, radius = Dimens.stickDotRadius, center = dot)
    }
}

// Each axis is normalised against its own travel, so a full diagonal reaches 1 on both and lands
// outside the ring. The stick's gate is round, so it's the magnitude that clamps, not each axis.
private fun dotPosition(centre: Offset, nx: Float, ny: Float, travel: Float): Offset {
    val magnitude = hypot(nx, ny)
    val scale = if (magnitude > 1f) 1f / magnitude else 1f
    return Offset(centre.x + nx * scale * travel, centre.y - ny * scale * travel)
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
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.stickAxisGap)) {
        Text(
            axis,
            color = TextDim,
            fontSize = Dimens.fontSizeLabel,
            style = AppType.telemetry,
        )
        Text(
            "%4d".format(value),
            color = TextBright,
            fontSize = Dimens.fontSizeLabel,
            style = AppType.telemetry,
        )
    }
}
