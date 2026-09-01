package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PacketParserTest {

    // A minimal but valid input report: left stick at 0x0A, right stick at 0x0D, and the Pro
    // Controller's back-paddle byte at 0x07 (GR = bit 0, GL = bit 1).
    private fun packet(
        leftStick: Pair<Int, Int> = 0x123 to 0x456,
        rightStick: Pair<Int, Int> = 0x789 to 0xABC,
        paddleByte: Int = 0,
    ): ByteArray = ByteArray(64).apply {
        putStick(0x0A, leftStick)
        putStick(0x0D, rightStick)
        this[0x07] = paddleByte.toByte()
    }

    private fun ByteArray.putStick(offset: Int, stick: Pair<Int, Int>) {
        val (x, y) = stick
        val v = (x and 0xFFF) or ((y and 0xFFF) shl 12)
        this[offset] = (v and 0xFF).toByte()
        this[offset + 1] = ((v shr 8) and 0xFF).toByte()
        this[offset + 2] = ((v shr 16) and 0xFF).toByte()
    }

    @Test
    fun `rejects packets below the minimum size`() {
        assertNull(PacketParser.parse(ByteArray(16), Side.PRO))
    }

    @Test
    fun `pro decodes both sticks from their own offsets`() {
        val input = PacketParser.parse(packet(), Side.PRO)!!
        assertEquals(0x123, input.stickX)
        assertEquals(0x456, input.stickY)
        assertEquals(0x789, input.rightStickX)
        assertEquals(0xABC, input.rightStickY)
    }

    @Test
    fun `a single left joy-con reports no right stick`() {
        val input = PacketParser.parse(packet(), Side.LEFT)!!
        assertEquals(0x123, input.stickX)
        assertEquals(2048, input.rightStickX)
        assertEquals(2048, input.rightStickY)
    }

    @Test
    fun `back paddles decode from byte 0x07`() {
        val input = PacketParser.parse(packet(paddleByte = 0x03), Side.PRO)!!
        assertTrue(JoyconButton.GR.id in input.pressed)
        assertTrue(JoyconButton.GL.id in input.pressed)
    }

    @Test
    fun `paddles are absent when their bits are clear`() {
        val input = PacketParser.parse(packet(paddleByte = 0x00), Side.PRO)!!
        assertTrue(JoyconButton.GR.id !in input.pressed)
        assertTrue(JoyconButton.GL.id !in input.pressed)
    }
}
