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

    private val side = JoyconSide.DUAL
    private val console = Console.SWITCH_PRO
    private val layouts = MappingLayouts.forBody(console, side, emptyList())
    private val default = MappingLayouts.entriesOf(console, side, SwitchProMapping)

    private fun changes(entries: Map<String, String>, motion: RemoteMotion = RemoteMotion()) =
        MappingLayouts.changesFromClosest(console, side, entries, motion, layouts)

    @Test
    fun `an unchanged layout has no changes`() {
        assertEquals(MappingChanges(), changes(default))
    }

    @Test
    fun `a rebound target is the only change`() {
        val rebound = default.keys.first { default.getValue(it).isNotEmpty() }

        assertEquals(setOf(rebound), changes(default + (rebound to "ZZ")).targets)
    }

    @Test
    fun `a cleared target is a change`() {
        val cleared = default.keys.first { default.getValue(it).isNotEmpty() }

        assertEquals(setOf(cleared), changes(default + (cleared to "")).targets)
    }

    @Test
    fun `a target the layout leaves empty stays unchanged when stored empty`() {
        assertEquals(MappingChanges(), changes(default + ("KEYBOARD_W" to "")))
    }

    @Test
    fun `changes are read against the closest layout, not the default`() {
        val wii = Console.WIIMOTE_NUNCHUK
        val lone = JoyconSide.RIGHT
        val closest = MarioKartWheelMapping
        val entries = MappingLayouts.entriesOf(wii, lone, closest)
        val rebound = entries.keys.first { entries.getValue(it).isNotEmpty() }

        val found = MappingLayouts.changesFromClosest(
            wii, lone, entries + (rebound to "ZZ"), closest.motion(lone), MappingLayouts.forBody(wii, lone, emptyList()),
        )

        assertEquals(setOf(rebound), found.targets)
    }

    @Test
    fun `motion shaping counts as a change`() {
        val found = changes(default, RemoteMotion(thrust = true, sensitivityPercent = 150))

        assertTrue(found.thrust)
        assertTrue(found.sensitivity)
        assertFalse(found.controller)
    }
}
