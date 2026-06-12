package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryGaugeTest {

    @Test
    fun `floor voltage reads empty`() {
        assertEquals(0, BatteryGauge.percentFromVolts(2.7f))
    }

    @Test
    fun `below floor voltage clamps to empty`() {
        assertEquals(0, BatteryGauge.percentFromVolts(2.4f))
    }

    @Test
    fun `full voltage reads full`() {
        assertEquals(100, BatteryGauge.percentFromVolts(3.6f))
    }

    @Test
    fun `above full voltage clamps to full`() {
        assertEquals(100, BatteryGauge.percentFromVolts(3.75f))
    }

    @Test
    fun `level boundaries map to quartiles`() {
        assertEquals(25, BatteryGauge.percentFromVolts(3.0f))
        assertEquals(50, BatteryGauge.percentFromVolts(3.16f))
        assertEquals(75, BatteryGauge.percentFromVolts(3.3f))
    }

    @Test
    fun `interpolates within a band`() {
        assertEquals(12, BatteryGauge.percentFromVolts(2.85f))
        assertEquals(88, BatteryGauge.percentFromVolts(3.45f))
    }

    @Test
    fun `matches charge levels observed on a switch 2`() {
        // Controllers reading ~3.59-3.61 V over BLE showed 100% when docked on a Switch 2.
        assertEquals(99, BatteryGauge.percentFromVolts(3.59f))
        assertEquals(100, BatteryGauge.percentFromVolts(3.61f))
    }
}
