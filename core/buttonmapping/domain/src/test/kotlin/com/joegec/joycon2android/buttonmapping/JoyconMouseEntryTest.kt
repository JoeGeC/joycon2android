package com.joegec.joycon2android.buttonmapping

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JoyconMouseEntryTest {

    @Test
    fun `unset, the mouse is on for Mouse & keys alone`() {
        assertTrue(emptyMap<String, String>().joyconMouse(Console.PC))
        assertFalse(emptyMap<String, String>().joyconMouse(Console.SWITCH_PRO))
    }

    @Test
    fun `switched on or off it follows the switch`() {
        assertTrue(mapOf(JOYCON_MOUSE_KEY to "true").joyconMouse(Console.GAMECUBE))
        assertFalse(mapOf(JOYCON_MOUSE_KEY to "false").joyconMouse(Console.PC))
    }

    @Test
    fun `the default is stored empty, so the body keeps matching its layout`() {
        assertEquals("", joyconMouseEntry(Console.SWITCH_PRO, on = false))
        assertEquals("true", joyconMouseEntry(Console.SWITCH_PRO, on = true))
        assertEquals("false", joyconMouseEntry(Console.PC, on = false))
    }

    @Test
    fun `a grip switch keeps the Joy-Con mouse choice`() {
        assertTrue(JOYCON_MOUSE_KEY in DEVICE_KEY_TARGETS)
    }
}
