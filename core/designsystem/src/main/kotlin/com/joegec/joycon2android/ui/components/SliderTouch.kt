package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/** True while a finger is on a slider, so the drawer's swipe and the panel's scroll hold still. */
class SliderTouch {
    var touching by mutableStateOf(false)
        private set

    internal fun update(touching: Boolean) {
        this.touching = touching
    }
}

val LocalSliderTouch = staticCompositionLocalOf<SliderTouch?> { null }

// Reads the Initial pass without consuming, so the slider still gets every event.
fun Modifier.reportsSliderTouch(touch: SliderTouch?): Modifier =
    if (touch == null) this
    else pointerInput(touch) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            touch.update(true)
            try {
                while (awaitPointerEvent(PointerEventPass.Initial).changes.any { it.pressed }) Unit
            } finally {
                touch.update(false)
            }
        }
    }
