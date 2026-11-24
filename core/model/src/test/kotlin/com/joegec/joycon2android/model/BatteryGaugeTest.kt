package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryGaugeTest {

    @Test
    fun `shutdown voltage reads empty`() {
        assertEquals(0, BatteryGauge.percentFromVolts(2.39f))
    }

    @Test
    fun `below shutdown voltage clamps to empty`() {
        assertEquals(0, BatteryGauge.percentFromVolts(2.2f))
    }

    @Test
    fun `a controller still running below the old floor reads above empty`() {
        assertEquals(1, BatteryGauge.percentFromVolts(2.80f))
    }

    @Test
    fun `fresh off the charger reads full`() {
        assertEquals(100, BatteryGauge.percentFromVolts(3.685f))
    }

    @Test
    fun `above full voltage clamps to full`() {
        assertEquals(100, BatteryGauge.percentFromVolts(3.75f))
    }

    @Test
    fun `measured anchors read their share of play time`() {
        assertEquals(5, BatteryGauge.percentFromVolts(3.09f))
        assertEquals(20, BatteryGauge.percentFromVolts(3.178f))
        assertEquals(50, BatteryGauge.percentFromVolts(3.279f))
        assertEquals(90, BatteryGauge.percentFromVolts(3.592f))
    }

    @Test
    fun `interpolates between anchors`() {
        assertEquals(55, BatteryGauge.percentFromVolts(3.312f))
    }

    @Test
    fun `the old gauge's half charge reads near empty`() {
        assertEquals(12, BatteryGauge.percentFromVolts(3.137f))
    }
}
