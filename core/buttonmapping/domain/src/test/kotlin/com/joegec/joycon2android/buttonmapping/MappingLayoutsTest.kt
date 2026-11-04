package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MarioKartNunchukMapping
import com.joegec.joycon2android.buttonmapping.preset.MarioKartWheelMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProSidewaysMapping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MappingLayoutsTest {

    @Test
    fun `the first suggestion is the first number`() {
        assertEquals("Custom 1", MappingLayouts.nextName("Custom", emptyList()))
    }

    @Test
    fun `each suggestion counts past the names already taken`() {
        assertEquals("Custom 3", MappingLayouts.nextName("Custom", listOf("Custom 1", "Custom 2")))
    }

    @Test
    fun `a number freed by a deleted layout is suggested again`() {
        assertEquals("Custom 2", MappingLayouts.nextName("Custom", listOf("Custom 1", "Custom 3")))
    }

    @Test
    fun `names of the user's own choosing stand in nobody's way`() {
        assertEquals("Custom 1", MappingLayouts.nextName("Custom", listOf("My Wheel", "Customised")))
    }

    @Test
    fun `a group of pairs isn't offered the sideways Pro Controller layouts`() {
        val offered = MappingLayouts.forGroup(Console.SWITCH_PRO, listOf(JoyconSide.DUAL))

        assertTrue(SwitchProMapping in offered)
        assertFalse(SwitchProSidewaysMapping in offered)
    }

    @Test
    fun `a group offers a layout once any body fits it`() {
        val offered = MappingLayouts.forGroup(Console.SWITCH_PRO, listOf(JoyconSide.DUAL, JoyconSide.LEFT))

        assertTrue(SwitchProSidewaysMapping in offered)
    }

    @Test
    fun `a group of pairs is offered the Nunchuk grip, not the Wheel`() {
        val offered = MappingLayouts.forGroup(Console.WIIMOTE_NUNCHUK, listOf(JoyconSide.DUAL))

        assertTrue(MarioKartNunchukMapping in offered)
        assertFalse(MarioKartWheelMapping in offered)
    }
}
