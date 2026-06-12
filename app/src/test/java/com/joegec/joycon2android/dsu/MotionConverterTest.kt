package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.JoyconInput
import org.junit.Assert.assertEquals
import org.junit.Test

class MotionConverterTest {

    private val tolerance = 1e-6f

    @Test
    fun `flat at rest - face-out gravity reads minus one g on accel Y`() {
        val motion = MotionConverter.convert(JoyconInput(accelZ = 4096))

        assertEquals(-1f, motion.accelY, tolerance)
        assertEquals(0f, motion.accelX, tolerance)
        assertEquals(0f, motion.accelZ, tolerance)
    }

    @Test
    fun `nose-up gravity (along minus raw Y) reads minus one g on accel Z`() {
        val motion = MotionConverter.convert(JoyconInput(accelY = -4096))

        assertEquals(-1f, motion.accelZ, tolerance)
    }

    @Test
    fun `left-side gravity maps straight onto accel X`() {
        val motion = MotionConverter.convert(JoyconInput(accelX = 4096))

        assertEquals(1f, motion.accelX, tolerance)
    }

    @Test
    fun `gyro maps to pitch yaw roll`() {
        val motion = MotionConverter.convert(JoyconInput(gyroX = 1000, gyroY = 1000, gyroZ = 1000))

        val dps = 1000 * 0.06103515625f
        assertEquals(dps, motion.gyroPitch, tolerance)
        assertEquals(-dps, motion.gyroYaw, tolerance)
        assertEquals(-dps, motion.gyroRoll, tolerance)
    }

    @Test
    fun `no input yields zero motion`() {
        assertEquals(DsuMotion(), MotionConverter.convert(null))
    }
}
