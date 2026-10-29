package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Rumble
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
            HdRumble.packet(counter = 3, high = 0x0C0, low = 0x0C0),
        )
    }

    @Test
    fun `the header counter wraps at 16`() {
        assertEquals(0x51.toByte(), HdRumble.packet(counter = 17, high = 0, low = 0)[1])
    }

    @Test
    fun `a Pro Controller packet repeats the group for its second motor`() {
        val packet = HdRumble.packet(counter = 0, high = 0x0C0, low = 0x0C0, motors = 2)

        assertEquals(33, packet.size)
        assertArrayEquals(packet.copyOfRange(1, 17), packet.copyOfRange(17, 33))
    }

    @Test
    fun `silence keeps the carrier frequencies at zero amplitude`() {
        assertArrayEquals(bytes(0x87, 0x01, 0x20, 0x11, 0x00), HdRumble.packet(counter = 0, high = 0, low = 0).copyOfRange(2, 7))
    }

    @Test
    fun `the strong motor drives the low band and the weak one the high band`() {
        val subframe = HdRumble.packet(counter = 0, Rumble(strong = 1f, weak = 0f)).copyOfRange(2, 7)

        assertArrayEquals(HdRumble.packet(counter = 0, high = 0, low = 0x1C4).copyOfRange(2, 7), subframe)
    }

    @Test
    fun `full strength is SDL's cap, not the actuator's maximum`() {
        val full = HdRumble.packet(counter = 0, Rumble(strong = 1f, weak = 1f))

        assertArrayEquals(HdRumble.packet(counter = 0, high = 0x1C4, low = 0x1C4), full)
    }

    @Test
    fun `a strength past 1 stays at the cap`() {
        val boosted = HdRumble.packet(counter = 0, Rumble(strong = 10f, weak = 10f))

        assertArrayEquals(HdRumble.packet(counter = 0, high = 0x1C4, low = 0x1C4), boosted)
    }

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }
}
