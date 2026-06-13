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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.ButtonOff
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
internal fun FaceButtons(
    pressed: Set<String>,
    modifier: Modifier = Modifier,
    buttonSize: Dp = Dimens.faceButtonSize,
    sideways: Boolean = false,
) {
    val offset = (buttonSize * 0.09f).coerceAtLeast(2.dp)
    Box(modifier.size(buttonSize * 3), contentAlignment = Alignment.Center) {
        if (sideways) {
            FaceButton(JoyconButton.Y.label, JoyconButton.Y.id in pressed, buttonSize, Modifier.align(Alignment.TopCenter).offset(y = offset))
            FaceButton(JoyconButton.X.label, JoyconButton.X.id in pressed, buttonSize, Modifier.align(Alignment.CenterEnd).offset(x = -offset))
            FaceButton(JoyconButton.A.label, JoyconButton.A.id in pressed, buttonSize, Modifier.align(Alignment.BottomCenter).offset(y = -offset))
            FaceButton(JoyconButton.B.label, JoyconButton.B.id in pressed, buttonSize, Modifier.align(Alignment.CenterStart).offset(x = offset))
        } else {
            FaceButton(JoyconButton.Y.label, JoyconButton.Y.id in pressed, buttonSize, Modifier.align(Alignment.CenterStart).offset(x = offset))
            FaceButton(JoyconButton.X.label, JoyconButton.X.id in pressed, buttonSize, Modifier.align(Alignment.TopCenter).offset(y = offset))
            FaceButton(JoyconButton.A.label, JoyconButton.A.id in pressed, buttonSize, Modifier.align(Alignment.CenterEnd).offset(x = -offset))
            FaceButton(JoyconButton.B.label, JoyconButton.B.id in pressed, buttonSize, Modifier.align(Alignment.BottomCenter).offset(y = -offset))
        }
    }
}

@Composable
private fun FaceButton(label: String, on: Boolean, buttonSize: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(buttonSize)
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
