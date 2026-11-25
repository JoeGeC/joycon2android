package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.BatteryCharge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SteadyBatteryTest {

    private val battery = SteadyBattery()

    private fun feed(percent: Int, times: Int = WINDOW): BatteryCharge? =
        (1..times).map { battery.steady(BatteryCharge(percent)) }.last()

    @Test
    fun `no charge is shown until a window of readings has arrived`() {
        assertNull(feed(13, times = WINDOW - 1))
    }

    @Test
    fun `a full window shows its charge`() {
        assertEquals(BatteryCharge(13), feed(13))
    }

    @Test
    fun `a brief sag does not move the charge`() {
        feed(13)

        assertEquals(BatteryCharge(13), feed(8, times = 5))
    }

    @Test
    fun `a sag in the first readings does not set the charge`() {
        feed(8, times = 5)

        assertEquals(BatteryCharge(13), feed(13, times = WINDOW - 5))
    }

    @Test
    fun `a reading wobbling within the noise keeps the shown charge`() {
        feed(80)

        assertEquals(BatteryCharge(80), feed(77))
    }

    @Test
    fun `a sustained reading past the noise is shown`() {
        feed(80)

        assertEquals(BatteryCharge(75), feed(75))
    }

    @Test
    fun `below five percent every change is shown`() {
        feed(6)

        assertEquals(BatteryCharge(4), feed(4))
        assertEquals(BatteryCharge(1), feed(1))
    }

    @Test
    fun `a reset starts a fresh window`() {
        feed(80)
        battery.reset()

        assertNull(feed(78, times = 1))
    }

    @Test
    fun `no reading clears the charge`() {
        feed(80)

        assertNull(battery.steady(null))
    }

    private companion object {
        const val WINDOW = 64
    }
}
