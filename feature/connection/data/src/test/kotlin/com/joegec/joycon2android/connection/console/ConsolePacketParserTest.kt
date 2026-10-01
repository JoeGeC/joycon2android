package com.joegec.joycon2android.connection.console

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.MotionSupport
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConsolePacketParserTest {

    // Captured from a NYXI Hyperion 3 (right) at rest: counter 2, power 0x18, stick centred.
    private val restingRight = hex("02 18 00 00 07 00 F8 7F 00 00 00 00 00 00 00 1E")

    // Captured flat, button face up, so gravity sits on z at 4096 = 1 g.
    private val flatRight = hex(
        "46 18 00 00 07 00 B8 7F 00 00 00 00 00 00 00 1E FF 86 01 0C 02 00 33 56 01 2F 0F 2C 00 " +
            "3A AB 89 00 00 02 01 00 00 84 FF 00 00 F6 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
            "00 00 00 00 00 00",
    )
    private val flatLeft = hex(
        "46 18 00 00 07 F1 17 82 00 00 00 00 00 00 1E FC 8E 01 0C 00 2E 88 BA 01 8E 52 08 01 A3 " +
            "ED 77 00 00 99 FF 00 00 1C 00 00 00 EF 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 " +
            "00 00 00 00 00 00",
    )

    private fun report(buttons: Int, power: Int = 0x18): ByteArray =
        restingRight.copyOf(63).apply {
            this[1] = power.toByte()
            this[2] = buttons.toByte()
            this[3] = (buttons shr 8).toByte()
        }

    @Test
    fun `rejects reports shorter than the stick field`() {
        assertNull(ConsolePacketParser.parse(ByteArray(7), Side.RIGHT, 1))
    }

    @Test
    fun `decodes the packed stick`() {
        val input = ConsolePacketParser.parse(restingRight, Side.RIGHT, 1)!!
        assertEquals(2048, input.stickX)
        assertEquals(2047, input.stickY)
    }

    @Test
    fun `right face and system buttons map onto the common bitmask`() {
        val input = ConsolePacketParser.parse(report(0b0101_0001_0000_0011), Side.RIGHT, 1)!!
        assertEquals(
            setOf(JoyconButton.B, JoyconButton.A, JoyconButton.Home, JoyconButton.Chat, JoyconButton.SrRight).map { it.id }.toSet(),
            input.pressed,
        )
    }

    @Test
    fun `left buttons map onto the common bitmask`() {
        val input = ConsolePacketParser.parse(report(0b1000_0001_1111_0001), Side.LEFT, 1)!!
        assertEquals(
            setOf(
                JoyconButton.Down, JoyconButton.L, JoyconButton.ZL, JoyconButton.Minus,
                JoyconButton.LS, JoyconButton.Capture, JoyconButton.SlLeft,
            ).map { it.id }.toSet(),
            input.pressed,
        )
    }

    @Test
    fun `battery level becomes a percentage of the nine levels`() {
        assertEquals(100, ConsolePacketParser.parse(report(0, power = 9 shl 2), Side.RIGHT, 1)!!.battery?.percent)
        assertEquals(55, ConsolePacketParser.parse(report(0, power = 5 shl 2), Side.RIGHT, 1)!!.battery?.percent)
    }

    @Test
    fun `decodes the accelerometer, gravity on z, with y against the raw axis`() {
        val input = ConsolePacketParser.parse(flatRight, Side.RIGHT, 1)!!
        assertEquals(258, input.accelX)
        assertEquals(124, input.accelY)
        assertEquals(4086, input.accelZ)
    }

    @Test
    fun `the left controller's motion block starts a byte earlier`() {
        val input = ConsolePacketParser.parse(flatLeft, Side.LEFT, 1)!!
        assertEquals(-103, input.accelX)
        assertEquals(-28, input.accelY)
        assertEquals(4079, input.accelZ)
    }

    @Test
    fun `reports an accelerometer without a gyroscope`() {
        assertEquals(MotionSupport.AccelerometerOnly, ConsolePacketParser.parse(flatRight, Side.RIGHT, 1)!!.motionSupport)
    }

    @Test
    fun `a block too short to hold motion reports none rather than a reading of zero`() {
        val starting = flatRight.copyOf().also { it[0x0F] = 0x04 }

        val input = ConsolePacketParser.parse(starting, Side.RIGHT, 1)!!

        assertEquals(MotionSupport.None, input.motionSupport)
        assertEquals(0, input.accelZ)
    }

    @Test
    fun `a motion block shaped some other way reports none`() {
        val foreign = flatRight.copyOf().also { it[0x20] = 0x01 }

        assertEquals(MotionSupport.None, ConsolePacketParser.parse(foreign, Side.RIGHT, 1)!!.motionSupport)
    }

    @Test
    fun `counter is the first byte`() {
        assertEquals(2, ConsolePacketParser.counter(restingRight))
    }

    private fun hex(value: String) = value.split(" ").map { it.toInt(16).toByte() }.toByteArray()
}
