package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.TextDim

private val allButtons = listOf(
    "A", "B", "X", "Y", "Up", "Down", "Left", "Right",
    "L", "R", "ZL", "ZR", "LS", "RS", "+", "-", "Home", "Camera", "Chat",
    "SL(L)", "SR(L)", "SL(R)", "SR(R)",
)

@Composable
internal fun ButtonsCard(pressed: Set<String>, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(16.dp)).padding(14.dp)
    ) {
        Text("BUTTONS", color = TextDim, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            allButtons.chunked(5).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { name ->
                        val on = name in pressed
                        Box(
                            Modifier
                                .weight(1f)
                                .height(34.dp)
                                .background(
                                    if (on) Accent else Color(0xFF0E1116),
                                    RoundedCornerShape(8.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                name,
                                color = if (on) Color(0xFF0E1116) else TextDim,
                                fontSize = 11.sp,
                                fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                    repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
