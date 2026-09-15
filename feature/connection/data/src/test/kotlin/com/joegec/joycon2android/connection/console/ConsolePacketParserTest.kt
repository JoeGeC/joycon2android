package com.joegec.joycon2android.connection.console

import com.joegec.joycon2android.model.BatteryGauge
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConsolePacketParserTest {

    // Captured from a NYXI Hyperion 3 (right) at rest: counter 2, power 0x18, stick centred.
    private val restingRight = hex("02 18 00 00 07 00 F8 7F 00 00 00 00 00 00 00 1E")

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
    fun `battery level becomes a voltage the gauge reads back as the same charge`() {
        val input = ConsolePacketParser.parse(report(0, power = 9 shl 2), Side.RIGHT, 1)!!
        assertEquals(100, BatteryGauge.percentFromVolts(input.batteryVolts))
    }

    @Test
    fun `counter is the first byte`() {
        assertEquals(2, ConsolePacketParser.counter(restingRight))
    }

    private fun hex(value: String) = value.split(" ").map { it.toInt(16).toByte() }.toByteArray()
}
