package com.joegec.joycon2android.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpiTriggerZerosTest {

    // Reply from an NSO GameCube controller to a 0x40-byte read at 0x013140 (Retroid Pocket Nova, 2026-10-07)
    private val reply = bytes("02 01 00 04 10 78 00 00 40 00 00 00 40 31 01 00 21 20" + " FF".repeat(62))

    @Test
    fun `the first two bytes are the left and right zero points`() {
        assertEquals(33 to 32, SpiTriggerZeros.parse(reply))
    }

    @Test
    fun `an unset zero reads as null`() {
        val unset = reply.copyOf().apply { this[16] = 0xFF.toByte() }
        assertEquals(null to 32, SpiTriggerZeros.parse(unset))
    }

    @Test
    fun `a reply for another address is not this read`() {
        val colour = reply.copyOf().apply { this[13] = 0x30 }
        assertNull(SpiTriggerZeros.parse(colour))
    }

    @Test
    fun `the colour parser still finds the accent in a device info block`() {
        val info = bytes("02 01 00 04 10 78 00 00 40 00 00 00 00 30 01 00" + " 00".repeat(0x1F) + " 12 34 56" + " 00".repeat(0x1D))
        assertEquals(0x123456, SpiColorParser.parseAccentColor(info))
    }

    private fun bytes(hex: String): ByteArray = hex.trim().split(" ").map { it.toInt(16).toByte() }.toByteArray()
}
