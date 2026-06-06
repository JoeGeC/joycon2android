package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.ButtonOff
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
internal fun FaceButtons(pressed: Set<String>, modifier: Modifier = Modifier) {
    Box(modifier.size(Dimens.faceButtonSize * 3), contentAlignment = Alignment.Center) {
        FaceButton("Y", "Y" in pressed, Modifier.align(Alignment.CenterStart).offset(x = 4.dp))
        FaceButton("X", "X" in pressed, Modifier.align(Alignment.TopCenter).offset(y = 4.dp))
        FaceButton("A", "A" in pressed, Modifier.align(Alignment.CenterEnd).offset(x = (-4).dp))
        FaceButton("B", "B" in pressed, Modifier.align(Alignment.BottomCenter).offset(y = (-4).dp))
    }
}

@Composable
private fun FaceButton(label: String, on: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(Dimens.faceButtonSize)
            .clip(CircleShape)
            .background(if (on) Accent else ButtonOff),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (on) TextOnAccent else Color.White,
            fontSize = Dimens.fontSizeFace,
            fontWeight = FontWeight.Bold,
        )
    }
}
