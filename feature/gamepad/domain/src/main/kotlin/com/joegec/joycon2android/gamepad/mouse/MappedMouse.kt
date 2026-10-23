package com.joegec.joycon2android.gamepad.mouse

import com.joegec.joycon2android.model.MouseAction

/** The mapping's mouse actions as a frame: clicks held, and a scroll that steps on press and repeats while held. */
class MappedMouse(private val nowMs: () -> Long) {

    private var scrolling = 0
    private var nextDetentAtMs = 0L

    fun frame(held: Set<MouseAction>): MouseFrame = MouseFrame(wheel = wheel(held), buttons = buttons(held))

    private fun wheel(held: Set<MouseAction>): Int {
        val direction = (if (MouseAction.SCROLL_UP in held) 1 else 0) - (if (MouseAction.SCROLL_DOWN in held) 1 else 0)
        val now = nowMs()
        val detent = when {
            direction == 0 -> 0
            direction != scrolling -> direction.also { nextDetentAtMs = now + REPEAT_MS }
            now >= nextDetentAtMs -> direction.also { nextDetentAtMs += REPEAT_MS }
            else -> 0
        }
        scrolling = direction
        return detent
    }

    private fun buttons(held: Set<MouseAction>): Set<MouseButton> = held.mapNotNullTo(mutableSetOf()) { action ->
        when (action) {
            MouseAction.LEFT_CLICK -> MouseButton.PRIMARY
            MouseAction.RIGHT_CLICK -> MouseButton.SECONDARY
            MouseAction.MIDDLE_CLICK -> MouseButton.MIDDLE
            MouseAction.SCROLL_UP, MouseAction.SCROLL_DOWN -> null
        }
    }

    private companion object {
        const val REPEAT_MS = 100L
    }
}
