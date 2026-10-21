package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingLayouts
import com.joegec.joycon2android.buttonmapping.RemoteMotion
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.classicController
import com.joegec.joycon2android.buttonmapping.directionKey
import com.joegec.joycon2android.buttonmapping.target.ClassicButton
import com.joegec.joycon2android.buttonmapping.target.ClassicStick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassicControllerMappingTest {

    @Test
    fun `every body can play the Classic Controller`() {
        JoyconSide.entries.forEach { side ->
            val layouts = MappingLayouts.forBody(Console.WIIMOTE_NUNCHUK, side, emptyList())

            assertTrue("$side", ClassicControllerMapping in layouts)
            assertTrue("$side", ClassicControllerMapping.entries(side).classicController)
        }
    }

    @Test
    fun `no other Wii layout plugs in a Classic Controller`() {
        MappingPresets.forConsole(Console.WIIMOTE_NUNCHUK).filterNot { it == ClassicControllerMapping }.forEach { preset ->
            preset.sides.forEach { side -> assertFalse("${preset.id} $side", preset.entries(side).classicController) }
        }
    }

    @Test
    fun `a pair or Pro Controller keeps every button where the Classic Controller Pro has it`() {
        val dual = ClassicControllerMapping.entries(JoyconSide.DUAL)

        assertEquals("A", dual.getValue(ClassicButton.ClassicA.name))
        assertEquals("ZL", dual.getValue(ClassicButton.ClassicZL.name))
        assertEquals("Minus", dual.getValue(ClassicButton.ClassicMinus.name))
        assertEquals("Up", dual.getValue(ClassicButton.ClassicDPadUp.name))
        assertEquals("LEFT_STICK_UP", dual.getValue(ClassicStick.ClassicLeftStick.directionKey(StickDirection.UP)))
        assertEquals("RIGHT_STICK_UP", dual.getValue(ClassicStick.ClassicRightStick.directionKey(StickDirection.UP)))
    }

    @Test
    fun `a GameCube controller has no minus, so Capture takes it`() {
        val gameCube = ClassicControllerMapping.entries(JoyconSide.GAMECUBE)

        assertEquals("Capture", gameCube.getValue(ClassicButton.ClassicMinus.name))
        assertEquals("ZR", gameCube.getValue(ClassicButton.ClassicZR.name))
    }

    @Test
    fun `a lone Joy-Con held sideways plays its stick as the left stick and leaves the d-pad unbound`() {
        val right = ClassicControllerMapping.entries(JoyconSide.RIGHT)

        assertEquals("X", right.getValue(ClassicButton.ClassicA.name))
        assertEquals("SrRight", right.getValue(ClassicButton.ClassicR.name))
        assertFalse(right.containsKey(ClassicButton.ClassicDPadUp.name))
        assertEquals("RIGHT_STICK_UP", right.getValue(ClassicStick.ClassicLeftStick.directionKey(StickDirection.UP)))
        assertFalse(right.containsKey(ClassicStick.ClassicRightStick.directionKey(StickDirection.UP)))
    }

    @Test
    fun `the Classic Controller reads back as its own name once applied`() {
        JoyconSide.entries.forEach { side ->
            val entries = MappingLayouts.entriesOf(Console.WIIMOTE_NUNCHUK, side, ClassicControllerMapping)

            val matched = MappingLayouts.matching(Console.WIIMOTE_NUNCHUK, side, entries, RemoteMotion(), emptyList())

            assertEquals("$side", ClassicControllerMapping, matched)
        }
    }
}
