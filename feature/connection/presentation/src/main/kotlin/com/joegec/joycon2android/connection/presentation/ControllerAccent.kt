package com.joegec.joycon2android.connection.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.TextOnAccent

/** Provided per [JoyconCard]: docs/DESIGN.md#color */
data class ControllerAccent(val color: Color, val onColor: Color)

val LocalControllerAccent = staticCompositionLocalOf { ControllerAccent(Accent, TextOnAccent) }
