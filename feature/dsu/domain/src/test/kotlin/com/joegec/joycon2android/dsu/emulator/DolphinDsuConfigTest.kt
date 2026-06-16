package com.joegec.joycon2android.dsu.emulator
import com.joegec.joycon2android.dsu.DsuConfig

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DolphinDsuConfigTest {

    private val ourEntry = "Joycon2:127.0.0.1:${DsuConfig.PORT}"

    @Test
    fun `null or blank produces a canonical enabled config with our entry`() {
        val result = DolphinDsuConfig.merge(null)
        assertTrue(result.contains("[Server]"))
        assertTrue(result.contains("Enabled = True"))
        assertTrue(result.contains("Entries = $ourEntry;"))
    }

    @Test
    fun `our entry is appended while existing servers are preserved`() {
        val existing = "[Server]\nEnabled = True\nEntries = Other:192.168.0.5:26760;"

        val result = DolphinDsuConfig.merge(existing)

        assertEquals("Entries = Other:192.168.0.5:26760;$ourEntry;", result.lines().last { it.startsWith("Entries") })
        assertTrue(result.contains("Other:192.168.0.5:26760"))
    }

    @Test
    fun `already present entry leaves the file untouched`() {
        val existing = "[Server]\nEnabled = True\nEntries = $ourEntry;"

        assertEquals(existing, DolphinDsuConfig.merge(existing))
    }

    @Test
    fun `a server section without an entries line is replaced with the canonical config`() {
        val existing = "[Server]\nEnabled = False"

        val result = DolphinDsuConfig.merge(existing)

        assertTrue(result.contains("Enabled = True"))
        assertTrue(result.contains("Entries = $ourEntry;"))
    }
}
