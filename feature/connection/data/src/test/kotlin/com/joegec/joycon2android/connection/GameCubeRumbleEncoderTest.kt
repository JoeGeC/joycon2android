package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Rumble
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class GameCubeRumbleEncoderTest {

    private val encoder = GameCubeRumbleEncoder()

    @Test
    fun `a packet is the report id, a counted header and the motor state`() {
        assertArrayEquals(bytes(0x00, 0x50, 0x01, 0x00, 0x00), encoder.packet(Rumble(1f, 0f)))
    }

    @Test
    fun `the header counter wraps at 16`() {
        repeat(17) { encoder.packet(Rumble.OFF) }

        assertEquals(0x51.toByte(), encoder.packet(Rumble.OFF)[1])
    }

    @Test
    fun `any strength on either motor runs the motor`() {
        assertEquals(ON, encoder.packet(Rumble(0f, 0.05f))[2])
    }

    @Test
    fun `off brakes the motor`() {
        assertEquals(BRAKE, encoder.packet(Rumble.OFF)[2])
    }

    @Test
    fun `a buzz runs the motor at any amplitude and brakes at zero`() {
        assertEquals(ON, encoder.packet(0x040)[2])
        assertEquals(BRAKE, encoder.packet(0)[2])
    }

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    private companion object {
        const val ON: Byte = 0x01
        const val BRAKE: Byte = 0x02
    }
}
