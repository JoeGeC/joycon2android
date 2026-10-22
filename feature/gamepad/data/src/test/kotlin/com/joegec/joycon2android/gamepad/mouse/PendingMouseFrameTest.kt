package com.joegec.joycon2android.gamepad.mouse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingMouseFrameTest {

    private val pending = PendingMouseFrame()

    @Test
    fun `movement between writes adds up`() {
        pending.add(MouseFrame(dx = 3, dy = 1, wheel = 1))
        pending.add(MouseFrame(dx = 4, dy = -1))

        assertEquals(MouseFrame(dx = 7, dy = 0, wheel = 1), pending.take())
    }

    @Test
    fun `nothing to send once taken`() {
        pending.add(MouseFrame(dx = 3))
        pending.take()

        assertNull(pending.take())
        assertFalse(pending.add(MouseFrame()))
    }

    @Test
    fun `a button change is sent without movement, once`() {
        assertTrue(pending.add(MouseFrame(buttons = setOf(MouseButton.PRIMARY))))
        assertEquals(MouseFrame(buttons = setOf(MouseButton.PRIMARY)), pending.take())

        assertFalse(pending.add(MouseFrame(buttons = setOf(MouseButton.PRIMARY))))
        assertTrue(pending.add(MouseFrame()))
        assertEquals(MouseFrame(), pending.take())
    }
}
