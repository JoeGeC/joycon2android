package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.AccentDim

@Composable
internal fun BatteryPill(volts: Float, label: String? = null, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(AccentDim, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        val text = if (label != null) "$label %.2f V".format(volts) else "%.2f V".format(volts)
        Text(
            text,
            color = Accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
    }
}
