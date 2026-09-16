package com.joegec.joycon2android.dsu.motion

import com.joegec.joycon2android.dsu.DsuSlots
import com.joegec.joycon2android.dsu.DsuStream
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Test

class SidewaysMotionTest {

    private val tolerance = 1e-6f

    private fun joycon(side: Side) = ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con")

    private fun solo(side: Side) = DsuStream(
        0,
        if (side == Side.LEFT) PlayerState(PlayerNumber.P1, left = joycon(side))
        else PlayerState(PlayerNumber.P1, right = joycon(side)),
    )

    private fun wire(stream: DsuStream, input: JoyconInput) =
        MotionConverter.convert(SidewaysMotion.orient(stream, input))

    // Held up like a wheel (face toward the player, rail on top), as Mario Kart 8 was measured.
    @Test
    fun `sideways right joycon tipped nose down leans the grip to the left`() {
        val motion = wire(solo(Side.RIGHT), JoyconInput(accelY = 4096))

        assertEquals(-1f, motion.accelX, tolerance)
        assertEquals(0f, motion.accelZ, tolerance)
    }

    @Test
    fun `sideways left joycon tipped nose down leans the grip to the right`() {
        val motion = wire(solo(Side.LEFT), JoyconInput(accelY = 4096))

        assertEquals(1f, motion.accelX, tolerance)
        assertEquals(0f, motion.accelZ, tolerance)
    }

    @Test
    fun `sideways right joycon reads its body's right on the grip's far axis`() {
        val motion = wire(solo(Side.RIGHT), JoyconInput(accelX = 4096))

        assertEquals(-1f, motion.accelZ, tolerance)
        assertEquals(0f, motion.accelX, tolerance)
    }

    @Test
    fun `sideways left joycon reads its body's left on the grip's far axis`() {
        val motion = wire(solo(Side.LEFT), JoyconInput(accelX = -4096))

        assertEquals(-1f, motion.accelZ, tolerance)
        assertEquals(0f, motion.accelX, tolerance)
    }

    @Test
    fun `the button face axis passes through`() {
        val input = JoyconInput(accelZ = 4096, gyroZ = 1000)

        assertEquals(input, SidewaysMotion.orient(solo(Side.RIGHT), input))
        assertEquals(input, SidewaysMotion.orient(solo(Side.LEFT), input))
    }

    @Test
    fun `gyro turns with the accelerometer`() {
        val input = JoyconInput(accelX = 1, accelY = 2, gyroX = 1, gyroY = 2)

        val right = SidewaysMotion.orient(solo(Side.RIGHT), input)
        val left = SidewaysMotion.orient(solo(Side.LEFT), input)

        assertEquals(right.accelX to right.accelY, right.gyroX to right.gyroY)
        assertEquals(left.accelX to left.accelY, left.gyroX to left.gyroY)
    }

    @Test
    fun `a pair and its second hand stay in the body frame`() {
        val pair = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))
        val input = JoyconInput(accelX = 1, accelY = 2, gyroX = 3, gyroY = 4)

        DsuSlots.streams(listOf(pair)).forEach { stream ->
            assertEquals(input, SidewaysMotion.orient(stream, input))
        }
    }
}
