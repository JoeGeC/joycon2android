package com.joegec.joycon2android.ui.theme

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor

val Accent = Color(0xFF38E0C8)
val AccentDim = Color(0xFF1C3A38)
val Background = Color(0xFF0E1116)
val CardBg = Color(0xFF161B22)
val TextDim = Color(0xFF8B98A5)
val ButtonOff = Color(0xFF1A1F26)
val ErrorBg = Color(0xFF2D1B1B)
val ErrorText = Color(0xFFFF6B6B)
val TextOnAccent = Color(0xFF0E1116)
val JoyconDefaultColor = Accent

val JoyconBlue = joyconBorderColor(0x9BE1E6, JoyconDefaultColor)
val JoyconRed = joyconBorderColor(0xFF8C5F, JoyconDefaultColor)

val StickBg = Color(0xFF0E1116)
val CrosshairColor = Color(0xFF222C36)

private const val ACCENT_SATURATION_BOOST = 1.4f

/**
 * The controller's real shell accent color (read from SPI flash, packed as 0xRRGGBB)
 */
fun joyconBorderColor(accentColor: Int?, fallback: Color): Color {
    if (accentColor == null) return fallback
    val hsv = FloatArray(3)
    AndroidColor.RGBToHSV(
        (accentColor shr 16) and 0xFF,
        (accentColor shr 8) and 0xFF,
        accentColor and 0xFF,
        hsv,
    )
    return Color.hsv(hsv[0], (hsv[1] * ACCENT_SATURATION_BOOST).coerceAtMost(1f), hsv[2])
}
