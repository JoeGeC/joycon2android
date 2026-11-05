package com.joegec.joycon2android.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Motion
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun StatusLine(status: String, lit: Boolean, modifier: Modifier = Modifier) {
    val color by animateColorAsState(if (lit) Accent else TextDim, Motion.easeOut(), label = "status")
    AnimatedContent(
        targetState = status,
        transitionSpec = { fadeIn(Motion.easeOut()) togetherWith fadeOut(Motion.easeOut()) },
        label = "status",
        modifier = modifier.fillMaxWidth(),
    ) { text ->
        Text(text, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}
