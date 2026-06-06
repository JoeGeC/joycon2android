package com.joegec.joycon2android.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun ScanningIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scan")
    val dots by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "dots",
    )

    Box(
        modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(12.dp)).padding(14.dp)
    ) {
        Column {
            Text(
                "Looking for Joy-Con 2" + ".".repeat(dots.toInt() + 1),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Make sure the controller is in pairing mode (SYNC button held)",
                color = TextDim,
                fontSize = 12.sp,
            )
        }
    }
}
