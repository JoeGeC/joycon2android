package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.JoyconInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StickCalibratorTest {

    private val calibrator = StickCalibrator(restWindowSize = 4, maxRestSpreadLsb = 8)

    // Hardware-measured left Joy-Con: rests off-centre, travels ~1200 LSB each way.
    private val restX = 2080
    private val fullLeft = 900
    private val fullRight = 3400

    private fun rescale(x: Int): Int = calibrator.calibrate(JoyconInput(stickX = x)).stickX

    private fun settleAtRest(value: Int = restX) = repeat(4) { rescale(value) }

    @Test
    fun `a stick resting off-centre reports dead centre once calibrated`() {
        settleAtRest()

        assertEquals(2048, rescale(restX))
    }

    @Test
    fun `full deflection reaches both ends of the range`() {
        settleAtRest()

        assertEquals(4095, rescale(fullRight))
        assertEquals(0, rescale(fullLeft))
    }

    @Test
    fun `each direction is scaled by its own span`() {
        settleAtRest()
        rescale(fullRight)
        rescale(fullLeft)

        // Rest sits 1180 above full left and 1320 below full right, so the same raw
        // distance from centre must not produce the same output on both sides.
        val right = rescale(restX + 600)
        val left = rescale(restX - 600)
        assertTrue(right - 2048 < 2048 - left)
    }

    @Test
    fun `a stick held at full deflection is never adopted as centre`() {
        settleAtRest()

        repeat(40) { rescale(fullRight) }

        assertEquals(4095, rescale(fullRight))
    }

    @Test
    fun `a window with the stick moving is rejected and the next still one is used`() {
        listOf(2080, 2600, 3100, 3400).forEach { rescale(it) }
        val beforeSettling = rescale(restX)

        settleAtRest()

        assertNotEquals(2048, beforeSettling)
        assertEquals(2048, rescale(restX))
    }

    @Test
    fun `travel beyond the seeded span still maps to the end of the range`() {
        val wide = StickCalibrator(restWindowSize = 4, maxRestSpreadLsb = 8, seedHalfSpan = 600)
        repeat(4) { wide.calibrate(JoyconInput(stickX = restX)) }

        assertEquals(4095, wide.calibrate(JoyconInput(stickX = fullRight)).stickX)
    }

    @Test
    fun `sticks deflect before centre is learned`() {
        assertTrue(rescale(fullRight) > 2048)
    }

    @Test
    fun `each axis calibrates independently`() {
        repeat(4) {
            calibrator.calibrate(JoyconInput(stickX = 2080, stickY = 2157, rightStickX = 2014))
        }

        val calibrated = calibrator.calibrate(
            JoyconInput(stickX = 2080, stickY = 2157, rightStickX = 2014),
        )
        assertEquals(2048, calibrated.stickX)
        assertEquals(2048, calibrated.stickY)
        assertEquals(2048, calibrated.rightStickX)
    }

    @Test
    fun `controllers with different resting centres both calibrate to centre`() {
        val other = StickCalibrator(restWindowSize = 4, maxRestSpreadLsb = 8)
        settleAtRest()
        repeat(4) { other.calibrate(JoyconInput(stickX = 2014)) }

        assertEquals(2048, rescale(restX))
        assertEquals(2048, other.calibrate(JoyconInput(stickX = 2014)).stickX)
    }
}
