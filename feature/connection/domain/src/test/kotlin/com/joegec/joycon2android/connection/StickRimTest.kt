package com.joegec.joycon2android.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class StickRimTest {

    private fun radius(stick: Pair<Int, Int>) = hypot(stick.first - 2048.0, stick.second - 2048.0) / 2048

    @Test
    fun `centre stays centre`() {
        assertEquals(2048 to 2048, StickRim.saturate(2048, 2048))
    }

    @Test
    fun `a push just short of the gate reaches full tilt`() {
        assertEquals(4095 to 2048, StickRim.saturate(2048 + 1860, 2048)) // 0.91 of the radius
        assertEquals(0 to 2048, StickRim.saturate(2048 - 1860, 2048))
    }

    @Test
    fun `a diagonal past the gate is clamped to the circle without turning`() {
        val stick = StickRim.saturate(2048 + 1600, 2048 + 1600)

        assertEquals(1.0, radius(stick), 0.001)
        assertEquals(stick.first, stick.second)
    }

    @Test
    fun `a half push is scaled up in proportion`() {
        val stick = StickRim.saturate(2048 + 900, 2048)

        assertEquals(2048 + 1000, stick.first) // 900 / 0.9
        assertEquals(2048, stick.second)
    }

    @Test
    fun `tilt keeps growing all the way to the rim`() {
        val tilts = (0..1800 step 40).map { radius(StickRim.saturate(2048, 2048 + it)) }

        assertTrue(tilts.zipWithNext().all { (a, b) -> b > a })
    }
}
