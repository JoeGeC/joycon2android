package com.joegec.joycon2android.gamepad.mouse

import com.joegec.joycon2android.model.MouseAction
import org.junit.Assert.assertEquals
import org.junit.Test

class MappedMouseTest {

    private var now = 0L
    private val mouse = MappedMouse { now }

    private fun wheelAt(ms: Long, vararg held: MouseAction): Int {
        now = ms
        return mouse.frame(held.toSet()).wheel
    }

    @Test
    fun `clicks hold their buttons`() {
        assertEquals(
            setOf(MouseButton.PRIMARY, MouseButton.MIDDLE),
            mouse.frame(setOf(MouseAction.LEFT_CLICK, MouseAction.MIDDLE_CLICK)).buttons,
        )
    }

    @Test
    fun `a scroll steps once on press, then every 100 ms while held`() {
        val detents = (0L..250L step 15).map { wheelAt(it, MouseAction.SCROLL_UP) }

        assertEquals(3, detents.sum())
        assertEquals(1, detents.first())
    }

    @Test
    fun `scroll down steps the other way, and a fresh press steps at once`() {
        wheelAt(0, MouseAction.SCROLL_DOWN)
        wheelAt(15)

        assertEquals(-1, wheelAt(30, MouseAction.SCROLL_DOWN))
    }

    @Test
    fun `up and down together cancel`() {
        assertEquals(0, wheelAt(0, MouseAction.SCROLL_UP, MouseAction.SCROLL_DOWN))
    }
}
