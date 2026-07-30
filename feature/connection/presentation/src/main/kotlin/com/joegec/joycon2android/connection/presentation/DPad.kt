package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.ui.theme.ButtonOff
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun DPad(
    pressed: Set<String>,
    modifier: Modifier = Modifier,
    buttonSize: Dp = Dimens.dpadSize,
    sideways: Boolean = false,
) {
    Box(modifier.size(buttonSize * 3), contentAlignment = Alignment.Center) {
        if (sideways) {
            DPadButton("▲", JoyconButton.Right.id in pressed, buttonSize, Modifier.align(Alignment.TopCenter))
            DPadButton("▼", JoyconButton.Left.id in pressed, buttonSize, Modifier.align(Alignment.BottomCenter))
            DPadButton("◀", JoyconButton.Up.id in pressed, buttonSize, Modifier.align(Alignment.CenterStart))
            DPadButton("▶", JoyconButton.Down.id in pressed, buttonSize, Modifier.align(Alignment.CenterEnd))
        } else {
            DPadButton("▲", JoyconButton.Up.id in pressed, buttonSize, Modifier.align(Alignment.TopCenter))
            DPadButton("▼", JoyconButton.Down.id in pressed, buttonSize, Modifier.align(Alignment.BottomCenter))
            DPadButton("◀", JoyconButton.Left.id in pressed, buttonSize, Modifier.align(Alignment.CenterStart))
            DPadButton("▶", JoyconButton.Right.id in pressed, buttonSize, Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
private fun DPadButton(symbol: String, on: Boolean, buttonSize: Dp, modifier: Modifier = Modifier) {
    val accent = LocalControllerAccent.current
    Box(
        modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(if (on) accent.color else ButtonOff),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = if (on) accent.onColor else Color.White, fontSize = Dimens.fontSizeDpad)
    }
}
