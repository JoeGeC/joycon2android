package com.joegec.joycon2android.ui.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
internal fun TriggerBar(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier.background(CardBg, RoundedCornerShape(16.dp)).padding(14.dp)) {
        Text(label, color = TextDim, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(8.dp))
        val fraction = (value / 255f).coerceIn(0f, 1f)
        Box(
            Modifier.fillMaxWidth().height(14.dp)
                .background(Color(0xFF0E1116), RoundedCornerShape(7.dp))
        ) {
            Box(
                Modifier.fillMaxWidth(fraction).height(14.dp)
                    .background(Accent, RoundedCornerShape(7.dp))
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("$value", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}
