package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.AccentDim
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
fun HintBubble(
    text: String,
    pointerCenterFromEnd: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        BubblePointer(Modifier.padding(end = pointerCenterFromEnd - Dimens.hintPointerWidth / 2))
        Box(
            Modifier
                .clip(RoundedCornerShape(Dimens.buttonCorner))
                .background(AccentDim)
                .padding(horizontal = Dimens.hintPaddingHorizontal, vertical = Dimens.hintPaddingVertical),
        ) {
            Text(text, color = Accent, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun BubblePointer(modifier: Modifier = Modifier) {
    Canvas(modifier.size(Dimens.hintPointerWidth, Dimens.hintPointerHeight)) {
        val upward = Path().apply {
            moveTo(size.width / 2, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(upward, AccentDim)
    }
}
