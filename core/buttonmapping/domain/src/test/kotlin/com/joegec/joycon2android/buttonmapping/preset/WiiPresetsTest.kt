package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingLayouts
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.emittedFor
import com.joegec.joycon2android.buttonmapping.sourceIdsOf
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WiiPresetsTest {

    @Test
    fun `the Wii layout is what the console starts on`() {
        assertEquals(WiiMapping, MappingPresets.default(Console.WIIMOTE_NUNCHUK))
    }

    @Test
    fun `the Joy-Con layout puts the remote's buttons where a Joy-Con keeps them`() {
        val dual = JoyconWiiMapping.entries(JoyconSide.DUAL)
        assertEquals("B", dual.getValue(WiimoteButton.B.name))
        assertEquals("R", dual.getValue(WiimoteButton.One.name))
        assertEquals("ZR", dual.getValue(WiimoteButton.Two.name))
        assertEquals("Minus", dual.getValue(WiimoteButton.Minus.name))

        val right = JoyconWiiMapping.entries(JoyconSide.RIGHT)
        assertEquals("B", right.getValue(WiimoteButton.B.name))
        assertEquals("R", right.getValue(WiimoteButton.One.name))
        assertEquals("ZR", right.getValue(WiimoteButton.Two.name))

        val left = JoyconWiiMapping.entries(JoyconSide.LEFT)
        assertEquals("Right", left.getValue(WiimoteButton.A.name))
        assertEquals("Down", left.getValue(WiimoteButton.B.name))
        assertEquals("L", left.getValue(WiimoteButton.One.name))
        assertEquals("ZL", left.getValue(WiimoteButton.Two.name))
        assertEquals("Up", left.getValue(WiimoteButton.Plus.name))
    }

    @Test
    fun `no button fires two of the Joy-Con layout's targets`() {
        JoyconSide.entries.forEach { side ->
            val fired = mutableMapOf<String, MutableSet<String>>()
            JoyconWiiMapping.entries(side).forEach { (target, value) ->
                sourceIdsOf(value).forEach { fired.getOrPut(it) { mutableSetOf() }.add(target) }
            }

            assertEquals("$side", emptyMap<String, Set<String>>(), fired.filterValues { it.size > 1 })
        }
    }

    @Test
    fun `Mario Kart accelerates and brakes on the buttons Mario Kart 8 uses`() {
        val right = MarioKartWheelMapping.entries(JoyconSide.RIGHT)

        assertEquals("X", right.getValue(WiimoteButton.Two.name))
        assertEquals("A", right.getValue(WiimoteButton.One.name))
        assertEquals("SrRight", right.getValue(WiimoteButton.B.name))
    }

    @Test
    fun `Mario Kart puts both bodies' jobs under the same thumb positions`() {
        val left = MarioKartWheelMapping.entries(JoyconSide.LEFT)

        // Sideways, the left Joy-Con's Down sits where the right's X does, Left where its A does,
        // Right where its Y does and Up where its B does (see SidewaysMapper).
        assertEquals("Down", left.getValue(WiimoteButton.Two.name))
        assertEquals("Left", left.getValue(WiimoteButton.One.name))
        assertEquals("Right", left.getValue(WiimoteButton.A.name))
        assertEquals("Up", left.getValue(WiimoteButton.Minus.name))
        assertEquals("Minus", left.getValue(WiimoteButton.Plus.name))
    }

    @Test
    fun `Mario Kart throws an item from SL as well as the stick`() {
        assertEquals("RIGHT_STICK_UP|SlRight", MarioKartWheelMapping.entries(JoyconSide.RIGHT).getValue("DPadUp"))
        assertEquals("LEFT_STICK_UP|SlLeft", MarioKartWheelMapping.entries(JoyconSide.LEFT).getValue("DPadUp"))
        assertEquals("LEFT_STICK_DOWN", MarioKartWheelMapping.entries(JoyconSide.LEFT).getValue("DPadDown"))
    }

    @Test
    fun `a lone Joy-Con on the Nunchuck layout plays both halves itself`() {
        JoyconSide.entries.filterNot { it == JoyconSide.DUAL }.forEach { side ->
            val lone = MarioKartNunchukMapping.entries(side)
            val rail = if (side == JoyconSide.LEFT) "SlLeft" else "SlRight"

            assertEquals("$side", rail, lone.getValue(WiimoteButton.NunchukZ.name))
            assertTrue("$side steers from its own stick", lone.keys.any { it.startsWith(WiimoteStick.NunchukStick.name) })
            // Unbound on purpose; omitted, each would keep the Wii layout's binding.
            listOf(
                WiimoteButton.DPadUp, WiimoteButton.DPadDown, WiimoteButton.DPadLeft, WiimoteButton.DPadRight,
                WiimoteButton.One, WiimoteButton.Two, WiimoteButton.Minus,
            ).forEach { assertEquals("$side ${it.name}", "", lone.getValue(it.name)) }
        }
    }

    @Test
    fun `the Nunchuck layout puts the same job under the same thumb on both bodies`() {
        val left = MarioKartNunchukMapping.entries(JoyconSide.LEFT)
        val right = MarioKartNunchukMapping.entries(JoyconSide.RIGHT)

        listOf(WiimoteButton.A, WiimoteButton.B, WiimoteButton.NunchukC).forEach { target ->
            assertEquals(
                target.name,
                emittedFace(left, target, JoyconSide.LEFT),
                emittedFace(right, target, JoyconSide.RIGHT),
            )
        }
    }

    private fun emittedFace(entries: Map<String, String>, target: WiimoteButton, side: JoyconSide) =
        (MappingSource.fromId(sourceIdsOf(entries.getValue(target.name)).first()) as? MappingSource.Button)
            ?.button
            ?.emittedFor(side)

    @Test
    fun `no button on a lone Joy-Con fires two targets, bar the shoulder that hops and tricks`() {
        JoyconSide.entries.filterNot { it == JoyconSide.DUAL }.forEach { side ->
            val entries = MappingLayouts.entriesOf(Console.WIIMOTE_NUNCHUK, side, MarioKartNunchukMapping)
            val fired = mutableMapOf<String, MutableSet<String>>()
            entries.forEach { (target, value) ->
                sourceIdsOf(value).forEach { fired.getOrPut(it) { mutableSetOf() }.add(target) }
            }
            val rail = if (side == JoyconSide.LEFT) "SrLeft" else "SrRight"

            assertEquals(
                "$side",
                mapOf(rail to setOf(WiimoteButton.B.name, WiimoteButton.Shake.name)),
                fired.filterValues { it.size > 1 },
            )
        }
    }

    @Test
    fun `the wheel is offered to a lone Joy-Con alone, the Nunchuck layout to every body`() {
        assertEquals(setOf(JoyconSide.LEFT, JoyconSide.RIGHT), MarioKartWheelMapping.sides)
        assertEquals(JoyconSide.entries.toSet(), MarioKartNunchukMapping.sides)
    }

    @Test
    fun `only the Mario Kart layouts play as a sideways Wii Remote`() {
        assertTrue(MarioKartWheelMapping.sidewaysRemote)
        assertTrue(MarioKartNunchukMapping.sidewaysRemote)
        assertFalse(WiiMapping.sidewaysRemote)
        assertFalse(JoyconWiiMapping.sidewaysRemote)
    }

    @Test
    fun `Mario Kart Nunchuck moves a pair's fingers onto the shoulders, and tricks from one`() {
        val pair = MarioKartNunchukMapping.entries(JoyconSide.DUAL)

        assertEquals("ZL", pair.getValue(WiimoteButton.One.name))
        assertEquals("ZR", pair.getValue(WiimoteButton.Two.name))
        assertEquals("R|B", pair.getValue(WiimoteButton.B.name))
        assertEquals("L", pair.getValue(WiimoteButton.NunchukZ.name))
        assertEquals("X", pair.getValue(WiimoteButton.NunchukC.name))
        assertEquals("Minus", pair.getValue(WiimoteButton.Minus.name))
        assertEquals("R", pair.getValue(WiimoteButton.Shake.name))
    }

    @Test
    fun `a pair has no sideways grip to match, so the rest stays the Wii layout`() {
        val untouched = WiiMapping.entries(JoyconSide.DUAL) - MarioKartNunchukMapping.entries(JoyconSide.DUAL).keys

        assertTrue(untouched.isEmpty())
        assertEquals(
            WiiMapping.entries(JoyconSide.DUAL).getValue(WiimoteButton.A.name),
            MarioKartNunchukMapping.entries(JoyconSide.DUAL).getValue(WiimoteButton.A.name),
        )
    }

    @Test
    fun `Mario Kart Wheel tricks off SR on a lone Joy-Con, the shoulder that already hops`() {
        JoyconSide.entries.filterNot { it == JoyconSide.DUAL }.forEach { side ->
            val lone = MarioKartWheelMapping.entries(side)

            assertEquals("$side", lone.getValue(WiimoteButton.B.name), lone.getValue(WiimoteButton.Shake.name))
        }
    }

    @Test
    fun `every Wii layout binds the whole remote on a lone Joy-Con`() {
        // Shake is a motion, not a button.
        val remote = (WiimoteButton.entries - WiimoteButton.NunchukC - WiimoteButton.NunchukZ -
            WiimoteButton.Shake).map { it.name }

        MappingPresets.forConsole(Console.WIIMOTE_NUNCHUK)
            .filterNot { it == MarioKartNunchukMapping }
            .forEach { preset ->
                assertTrue(
                    "${preset.id} binds the remote",
                    preset.entries(JoyconSide.RIGHT).keys.containsAll(remote),
                )
            }
    }
}
