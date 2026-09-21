package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
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
    fun `the Joy-Con layout swaps the remote's B and 2 on every body`() {
        JoyconSide.entries.forEach { side ->
            val wii = WiiMapping.entries(side)
            val joycon = JoyconWiiMapping.entries(side)

            assertEquals("B on $side", wii.getValue(WiimoteButton.Two.name), joycon.getValue(WiimoteButton.B.name))
            assertEquals("2 on $side", wii.getValue(WiimoteButton.B.name), joycon.getValue(WiimoteButton.Two.name))
        }
    }

    @Test
    fun `Mario Kart accelerates and brakes on the buttons Mario Kart 8 uses`() {
        val right = MarioKartWiiMapping.entries(JoyconSide.RIGHT)

        assertEquals("X", right.getValue(WiimoteButton.Two.name))
        assertEquals("A", right.getValue(WiimoteButton.One.name))
        assertEquals("SrRight", right.getValue(WiimoteButton.B.name))
    }

    @Test
    fun `Mario Kart puts both bodies' jobs under the same thumb positions`() {
        val left = MarioKartWiiMapping.entries(JoyconSide.LEFT)

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
        assertEquals("RIGHT_STICK_UP|SlRight", MarioKartWiiMapping.entries(JoyconSide.RIGHT).getValue("DPadUp"))
        assertEquals("LEFT_STICK_UP|SlLeft", MarioKartWiiMapping.entries(JoyconSide.LEFT).getValue("DPadUp"))
        assertEquals("LEFT_STICK_DOWN", MarioKartWiiMapping.entries(JoyconSide.LEFT).getValue("DPadDown"))
    }

    @Test
    fun `only the Mario Kart layout plays as a sideways Wii Remote`() {
        assertTrue(MarioKartWiiMapping.sidewaysRemote)
        assertFalse(WiiMapping.sidewaysRemote)
        assertFalse(JoyconWiiMapping.sidewaysRemote)
    }

    @Test
    fun `a pair has no sideways grip to match, so Mario Kart leaves it on the Wii layout`() {
        assertEquals(WiiMapping.entries(JoyconSide.DUAL), MarioKartWiiMapping.entries(JoyconSide.DUAL))
    }

    @Test
    fun `every Wii layout binds the whole remote on a lone Joy-Con`() {
        val remote = (WiimoteButton.entries - WiimoteButton.NunchukC - WiimoteButton.NunchukZ).map { it.name }

        MappingPresets.forConsole(Console.WIIMOTE_NUNCHUK).forEach { preset ->
            assertTrue("${preset.displayName} binds the remote", preset.entries(JoyconSide.RIGHT).keys.containsAll(remote))
        }
    }
}
