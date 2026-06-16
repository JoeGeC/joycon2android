package com.joegec.joycon2android.dsu.motion

import com.joegec.joycon2android.model.JoyconInput
import org.junit.Assert.assertEquals
import org.junit.Test

class GyroCalibratorTest {

    private val calibrator = GyroCalibrator(windowSize = 4, maxSpreadLsb = 10)

    @Test
    fun `passes input through before any still window completes`() {
        val input = JoyconInput(gyroX = 15, gyroY = -8, gyroZ = 3)

        assertEquals(input, calibrator.calibrate("A", input))
    }

    @Test
    fun `subtracts the bias learned from a still window`() {
        val resting = JoyconInput(gyroX = 15, gyroY = -8, gyroZ = 3)
        repeat(4) { calibrator.calibrate("A", resting) }

        val moving = calibrator.calibrate("A", JoyconInput(gyroX = 115, gyroY = -8, gyroZ = 53))

        assertEquals(100, moving.gyroX)
        assertEquals(0, moving.gyroY)
        assertEquals(50, moving.gyroZ)
    }

    @Test
    fun `a moving window does not become the bias`() {
        repeat(2) {
            calibrator.calibrate("A", JoyconInput(gyroX = 100))
            calibrator.calibrate("A", JoyconInput(gyroX = -100))
        }

        val input = JoyconInput(gyroX = 40)

        assertEquals(input, calibrator.calibrate("A", input))
    }

    @Test
    fun `controllers calibrate independently`() {
        repeat(4) { calibrator.calibrate("A", JoyconInput(gyroX = 20)) }

        val other = JoyconInput(gyroX = 20)

        assertEquals(other, calibrator.calibrate("B", other))
        assertEquals(0, calibrator.calibrate("A", JoyconInput(gyroX = 20)).gyroX)
    }

    @Test
    fun `a later still window replaces the bias`() {
        repeat(4) { calibrator.calibrate("A", JoyconInput(gyroX = 20)) }
        repeat(4) { calibrator.calibrate("A", JoyconInput(gyroX = 30)) }

        assertEquals(0, calibrator.calibrate("A", JoyconInput(gyroX = 30)).gyroX)
    }
}
