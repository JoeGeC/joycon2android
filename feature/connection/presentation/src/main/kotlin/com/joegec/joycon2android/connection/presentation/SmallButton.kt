package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.ui.theme.ButtonOff
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun SmallButton(label: String, on: Boolean, modifier: Modifier = Modifier) {
    val accent = LocalControllerAccent.current
    Box(
        modifier
            .height(Dimens.smallButtonHeight)
            .width(Dimens.smallButtonWidth)
            .background(if (on) accent.color else ButtonOff, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (on) accent.onColor else Color.White,
            fontSize = Dimens.fontSizeButton,
            fontWeight = FontWeight.Bold,
        )
    }
}
