package com.joegec.joycon2android.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

private const val PUSH_DURATION_MS = 300
private const val RECEDE_FRACTION = 4

/** Forward slides the new screen in over a receding one; back slides it away to reveal what's beneath. */
fun <S> AnimatedContentTransitionScope<S>.pushTransition(forward: Boolean): ContentTransform {
    val spec = tween<IntOffset>(PUSH_DURATION_MS)
    val fade = tween<Float>(PUSH_DURATION_MS)
    return if (forward) {
        (slideInHorizontally(spec) { it } togetherWith
            slideOutHorizontally(spec) { -it / RECEDE_FRACTION } + fadeOut(fade))
            .apply { targetContentZIndex = 1f }
    } else {
        (slideInHorizontally(spec) { -it / RECEDE_FRACTION } + fadeIn(fade) togetherWith
            slideOutHorizontally(spec) { it })
            .apply { targetContentZIndex = -1f }
    }
}
