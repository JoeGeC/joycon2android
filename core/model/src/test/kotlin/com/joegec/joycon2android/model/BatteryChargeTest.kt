package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryChargeTest {

    @Test
    fun `a voltage becomes the gauge percentage`() {
        assertEquals(75, BatteryCharge.fromVolts(3.30f)?.percent)
    }

    @Test
    fun `a packet without a voltage reports no charge`() {
        assertNull(BatteryCharge.fromVolts(0f))
    }

    @Test
    fun `a level becomes a percentage of the levels available`() {
        assertEquals(100, BatteryCharge.fromLevel(9, 9).percent)
        assertEquals(55, BatteryCharge.fromLevel(5, 9).percent)
        assertEquals(0, BatteryCharge.fromLevel(0, 9).percent)
    }
}
