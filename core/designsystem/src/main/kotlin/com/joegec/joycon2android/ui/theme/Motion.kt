package com.joegec.joycon2android.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

/** Ease-out, no bounce: docs/DESIGN.md#motion. Compose scales it by the system's animation setting. */
object Motion {
    const val DURATION_MS = 220

    fun <T> easeOut(): FiniteAnimationSpec<T> = tween(DURATION_MS, easing = LinearOutSlowInEasing)
}
