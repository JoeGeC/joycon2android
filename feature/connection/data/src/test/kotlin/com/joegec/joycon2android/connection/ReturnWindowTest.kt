package com.joegec.joycon2android.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ReturnWindowTest {

    private val scheduler = ManualScheduler()
    private var expiries = 0
    private val window = ReturnWindow(windowMs = 1_000, scheduler = scheduler) { expiries++ }

    @Test
    fun `expires once the window passes`() {
        window.open()
        scheduler.advanceBy(1_000)

        assertEquals(1, expiries)
        assertFalse(window.isOpen)
    }

    @Test
    fun `a return closes it without expiring`() {
        window.open()
        window.close()
        scheduler.advanceBy(1_000)

        assertEquals(0, expiries)
    }

    @Test
    fun `a second drop after a return gets a full window of its own`() {
        window.open()
        scheduler.advanceBy(600)
        window.close()
        window.open()
        scheduler.advanceBy(600)
        assertEquals(0, expiries)

        scheduler.advanceBy(400)
        assertEquals(1, expiries)
    }

    @Test
    fun `a failed attempt while open keeps the original deadline`() {
        window.open()
        scheduler.advanceBy(600)
        window.open()
        scheduler.advanceBy(400)

        assertEquals(1, expiries)
    }
}
