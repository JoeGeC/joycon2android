package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.BatteryCharge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SteadyBatteryTest {

    private val battery = SteadyBattery()

    @Test
    fun `a reading wobbling within the noise keeps the first one`() {
        battery.steady(BatteryCharge(80))

        listOf(81, 79, 82, 78).forEach { assertEquals(BatteryCharge(80), battery.steady(BatteryCharge(it))) }
    }

    @Test
    fun `a reading past the noise is shown`() {
        battery.steady(BatteryCharge(80))

        assertEquals(BatteryCharge(75), battery.steady(BatteryCharge(75)))
    }

    @Test
    fun `a reset shows the next reading as it is`() {
        battery.steady(BatteryCharge(80))
        battery.reset()

        assertEquals(BatteryCharge(78), battery.steady(BatteryCharge(78)))
    }

    @Test
    fun `no reading clears the charge`() {
        battery.steady(BatteryCharge(80))

        assertNull(battery.steady(null))
    }
}
