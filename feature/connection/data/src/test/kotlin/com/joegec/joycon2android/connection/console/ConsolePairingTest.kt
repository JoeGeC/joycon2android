package com.joegec.joycon2android.connection.console

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsolePairingTest {

    // Reply data captured from a NYXI Hyperion 3: status byte, then the 16-byte key.
    private val b1Reply = hex("01 5C F6 EE 79 2C DF 05 E1 BA 2B 63 25 C4 1A 5F 10")
    private val b2Reply = hex("01 13 4C 97 F5 11 B9 B6 DD 4D 86 FD 40 F5 36 E9 ED")

    @Test
    fun `address is byte-reversed and the second address drops the lowest byte by one`() {
        val encoded = ConsolePairing.encodeAddress("12:34:56:78:9A:BC")!!
        assertArrayEquals(hex("BC 9A 78 56 34 12"), encoded)
        assertArrayEquals(hex("BB 9A 78 56 34 12"), ConsolePairing.secondAddress(encoded))
    }

    @Test
    fun `rejects malformed addresses`() {
        assertNull(ConsolePairing.encodeAddress("02:00:00:00:00"))
    }

    @Test
    fun `long-term key is A1 xor B1`() {
        val ltk = ConsolePairing.ltk(ConsolePairing.b1From(b1Reply))
        assertArrayEquals(hex("69 F5 07 50 AE 58 74 C5 04 83 6F 43 82 0F DC 5B"), ltk)
    }

    @Test
    fun `controller confirmation matches the derived key`() {
        val ltk = ConsolePairing.ltk(ConsolePairing.b1From(b1Reply))
        assertTrue(ConsolePairing.confirms(ltk, b2Reply))
        assertFalse(ConsolePairing.confirms(ltk, null))
    }

    private fun hex(value: String) = value.split(" ").map { it.toInt(16).toByte() }.toByteArray()
}
