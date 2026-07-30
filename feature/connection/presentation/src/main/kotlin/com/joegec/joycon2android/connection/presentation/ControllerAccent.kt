package com.joegec.joycon2android.connection.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.TextOnAccent

/**
 * The colour a controller's live inputs light up in — its real shell colour — with the ink that
 * stays readable on it. [JoyconCard] provides one per controller so each player's buttons and
 * stick glow in that controller's own colour; the default is the teal accent for anything drawn
 * outside a card.
 */
data class ControllerAccent(val color: Color, val onColor: Color)

val LocalControllerAccent = staticCompositionLocalOf { ControllerAccent(Accent, TextOnAccent) }
