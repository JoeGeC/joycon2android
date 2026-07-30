package com.joegec.joycon2android.ui.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * App-specific text roles the Material type scale doesn't cover. These live outside [Typography]
 * because they aren't part of the reading hierarchy: [telemetry] is data-viz, [statusOverline] is
 * a fixed chrome label.
 */
object AppType {
    /**
     * Live numeric readouts (IMU, stick coordinates, battery %, DSU port, config snippets).
     * Tabular figures (`tnum`) keep digit columns aligned as values change; padding is stripped so
     * the mono line sits tight against the graphics it annotates. Size is intentionally left to the
     * call site — telemetry is sized to the control it labels, not to a hierarchy step.
     */
    val telemetry = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontFeatureSettings = "tnum",
        platformStyle = PlatformTextStyle(includeFontPadding = false),
    )

    /**
     * Wide-tracked chrome label for the connection/Shizuku status line in the app bar. Line height
     * is left at the font default so the two stacked status lines keep their breathing room.
     */
    val statusOverline = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
    )
}
