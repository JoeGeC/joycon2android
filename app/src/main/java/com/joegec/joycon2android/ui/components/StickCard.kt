package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.AccentDim
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun StickCard(x: Int, y: Int, pressed: Boolean, modifier: Modifier = Modifier) {
    val nx = (x - 2048f) / 2048f
    val ny = (y - 2048f) / 2048f
    val ringColor = if (pressed) Accent else AccentDim

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(90.dp)) {
            val r = size.minDimension / 2f
            val c = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = Color(0xFF0E1116), radius = r, center = c)
            drawCircle(color = ringColor, radius = r, center = c, style = Stroke(if (pressed) 3f else 2f))
            drawLine(Color(0xFF222C36), Offset(c.x - r, c.y), Offset(c.x + r, c.y), 1f)
            drawLine(Color(0xFF222C36), Offset(c.x, c.y - r), Offset(c.x, c.y + r), 1f)
            val dot = Offset(c.x + nx * r * 0.85f, c.y - ny * r * 0.85f)
            drawCircle(color = Accent, radius = 8f, center = dot)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "$x, $y",
            color = TextDim,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}
