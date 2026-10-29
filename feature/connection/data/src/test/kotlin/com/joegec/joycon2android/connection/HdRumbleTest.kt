package com.joegec.joycon2android.connection

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class HdRumbleTest {

    // SDL's EncodeHDRumble for frequencies 0x187 and 0x112 at amplitude 0x0C0.
    private val subframe = bytes(0x87, 0x01, 0x23, 0x11, 0x30)

    @Test
    fun `a Joy-Con packet is the report id, a counted header and three subframes`() {
        assertArrayEquals(
            bytes(0x00, 0x53) + subframe + subframe + subframe,
            HdRumble.packet(counter = 3, amplitude = 0x0C0),
        )
    }

    @Test
    fun `the header counter wraps at 16`() {
        assertEquals(0x51.toByte(), HdRumble.packet(counter = 17, amplitude = 0)[1])
    }

    @Test
    fun `a Pro Controller packet repeats the group for its second motor`() {
        val packet = HdRumble.packet(counter = 0, amplitude = 0x0C0, motors = 2)

        assertEquals(33, packet.size)
        assertArrayEquals(packet.copyOfRange(1, 17), packet.copyOfRange(17, 33))
    }

    @Test
    fun `silence keeps the carrier frequencies at zero amplitude`() {
        assertArrayEquals(bytes(0x87, 0x01, 0x20, 0x11, 0x00), HdRumble.packet(counter = 0, amplitude = 0).copyOfRange(2, 7))
    }

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }
}
