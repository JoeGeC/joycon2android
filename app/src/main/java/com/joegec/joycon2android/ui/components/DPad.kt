package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.ButtonOff
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
internal fun DPad(pressed: Set<String>, modifier: Modifier = Modifier) {
    Box(modifier.size(Dimens.dpadSize * 3), contentAlignment = Alignment.Center) {
        DPadButton("▲", "Up" in pressed, Modifier.align(Alignment.TopCenter))
        DPadButton("▼", "Down" in pressed, Modifier.align(Alignment.BottomCenter))
        DPadButton("◀", "Left" in pressed, Modifier.align(Alignment.CenterStart))
        DPadButton("▶", "Right" in pressed, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun DPadButton(symbol: String, on: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(Dimens.dpadSize)
            .clip(CircleShape)
            .background(if (on) Accent else ButtonOff),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = if (on) TextOnAccent else TextDim, fontSize = Dimens.fontSizeDpad)
    }
}
