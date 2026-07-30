package com.joegec.joycon2android.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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

val BatteryHigh = Accent
val BatteryMedium = Color(0xFFFBBF24)
val BatteryLow = ErrorText

private const val BATTERY_LOW_PERCENT = 20
private const val BATTERY_MEDIUM_PERCENT = 50

fun batteryColor(percent: Int): Color = when {
    percent <= BATTERY_LOW_PERCENT -> BatteryLow
    percent <= BATTERY_MEDIUM_PERCENT -> BatteryMedium
    else -> BatteryHigh
}

private const val ACCENT_SATURATION_BOOST = 1.4f

// A near-black shell would vanish when it fills a pressed control on the dark UI, so the active
// variant floors brightness while the thin border keeps the colour verbatim.
private const val ACTIVE_VALUE_FLOOR = 0.72f

// accentColor is the controller's real shell accent read from SPI flash, packed as 0xRRGGBB.
private fun boostedShellColor(accentColor: Int, valueFloor: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.RGBToHSV(
        (accentColor shr 16) and 0xFF,
        (accentColor shr 8) and 0xFF,
        accentColor and 0xFF,
        hsv,
    )
    return Color.hsv(
        hsv[0],
        (hsv[1] * ACCENT_SATURATION_BOOST).coerceAtMost(1f),
        hsv[2].coerceAtLeast(valueFloor),
    )
}

/** The controller's shell colour as a hairline border; falls back when the shell reports no colour. */
fun joyconBorderColor(accentColor: Int?, fallback: Color): Color =
    if (accentColor == null) fallback else boostedShellColor(accentColor, valueFloor = 0f)

/** The shell colour raised to a brightness floor so it still reads as "lit" filling a pressed control. */
fun controllerActiveColor(accentColor: Int?, fallback: Color = JoyconDefaultColor): Color =
    if (accentColor == null) fallback else boostedShellColor(accentColor, ACTIVE_VALUE_FLOOR)

/** Dark ink or white, whichever has the higher WCAG contrast against [background]. */
fun readableInkOn(background: Color): Color =
    if (contrastRatio(TextOnAccent, background) >= contrastRatio(Color.White, background)) {
        TextOnAccent
    } else {
        Color.White
    }

private fun contrastRatio(a: Color, b: Color): Float {
    val lighter = maxOf(a.luminance(), b.luminance())
    val darker = minOf(a.luminance(), b.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}
