package com.joegec.joycon2android.ui.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** The two text roles that aren't reading hierarchy, so aren't in the scale: docs/DESIGN.md#typography. */
object AppType {
    /** Sized by the caller: telemetry is sized to the control it labels, not to a hierarchy step. */
    val telemetry = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontFeatureSettings = "tnum",
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

    /** Line height stays at the font default, so the two stacked status lines keep their room. */
    val statusOverline = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
    )
}
