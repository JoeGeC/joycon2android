package com.joegec.joycon2android.ble

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JoyconAdvertisementTest {

    // Captured from a right Joy-Con 2: SYNC held (pairing) vs A pressed (wake)
    private val pairing = bytes("01 00 03 7E 05 66 20 00 01 00 00 00 00 00 00 00 0F 00 00 00 00 00 00 00")
    private val wake = bytes("01 00 03 7E 05 66 20 00 01 00 09 A7 9A 55 E2 98 0F 00 00 00 00 00 00 00")

    @Test
    fun `pairing-mode advertisement has a zeroed host MAC`() {
        assertTrue(JoyconAdvertisement.isPairing(pairing))
    }

    @Test
    fun `button-press wake advertisement carries the bonded host MAC`() {
        assertFalse(JoyconAdvertisement.isPairing(wake))
    }

    @Test
    fun `short or unknown layouts are not blocked`() {
        assertTrue(JoyconAdvertisement.isPairing(ByteArray(4)))
    }

    private fun bytes(hex: String): ByteArray =
        hex.split(" ").map { it.toInt(16).toByte() }.toByteArray()
}
