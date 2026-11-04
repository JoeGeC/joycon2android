package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.output.OutputDevice.KEYBOARD
import com.joegec.joycon2android.output.OutputDevice.MOTION
import com.joegec.joycon2android.output.OutputDevice.MOUSE
import com.joegec.joycon2android.output.OutputDevice.PAD
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OutputPlanTest {

    @Test
    fun `each tab runs the device it is named for`() {
        assertEquals(setOf(PAD), OutputPlan.devices(setOf(OutputMode.GAMEPAD), emptySet()))
        assertEquals(setOf(MOTION), OutputPlan.devices(setOf(OutputMode.MOTION), emptySet()))
        assertEquals(setOf(KEYBOARD, MOUSE), OutputPlan.devices(setOf(OutputMode.MOUSE_KEYBOARD), emptySet()))
    }

    @Test
    fun `tabs running together run all their devices`() {
        assertEquals(setOf(PAD, MOTION), OutputPlan.devices(setOf(OutputMode.GAMEPAD, OutputMode.MOTION), emptySet()))
    }

    @Test
    fun `a mapping that presses keys or clicks brings that device up too`() {
        assertEquals(setOf(MOTION, KEYBOARD), OutputPlan.devices(setOf(OutputMode.MOTION), setOf(KEYBOARD)))
        assertEquals(setOf(PAD, MOUSE), OutputPlan.devices(setOf(OutputMode.GAMEPAD), setOf(MOUSE)))
    }

    @Test
    fun `with no tab running nothing runs, mapped keys included`() {
        assertEquals(emptySet<OutputDevice>(), OutputPlan.devices(emptySet(), setOf(KEYBOARD, MOUSE)))
    }

    @Test
    fun `each tab maps its emulator's console, and with none only the app's keys`() {
        assertEquals(Console.WIIMOTE_NUNCHUK, OutputPlan.console(OutputMode.MOTION, EmulatorKind.DOLPHIN_WII))
        assertEquals(Console.SWITCH_PRO, OutputPlan.console(OutputMode.GAMEPAD, EmulatorKind.EDEN))
        assertEquals(Console.ANDROID, OutputPlan.console(OutputMode.GAMEPAD, EmulatorKind.OTHER))
        assertEquals(Console.ANDROID, OutputPlan.console(OutputMode.GAMEPAD, null))
        assertEquals(Console.PC, OutputPlan.console(OutputMode.MOUSE_KEYBOARD, EmulatorKind.EDEN))
    }

    @Test
    fun `each tab keeps its own emulator`() {
        val selection = OutputSelection(OutputMode.MOTION, mapOf(OutputMode.GAMEPAD to "eden", OutputMode.MOTION to "dolphin-wii"))

        assertEquals("dolphin-wii", selection.emulatorId)
        assertEquals("eden", selection.copy(mode = OutputMode.GAMEPAD).emulatorId)
    }

    @Test
    fun `Eden set up from both tabs collides, Dolphin's two consoles and two Edens don't`() {
        val eden = EmulatorTarget("eden", "dev.eden.eden_emulator", "Eden", EmulatorKind.EDEN)
        val nightly = EmulatorTarget("eden-nightly", "dev.eden.eden_nightly", "Eden Nightly", EmulatorKind.EDEN)
        val gameCube = EmulatorTarget("dolphin-gamecube", "org.dolphinemu.dolphinemu", "Dolphin (GameCube)", EmulatorKind.DOLPHIN_GAMECUBE)
        val wii = EmulatorTarget("dolphin-wii", "org.dolphinemu.dolphinemu", "Dolphin (Wii)", EmulatorKind.DOLPHIN_WII)

        assertTrue(OutputPlan.setupsCollide(eden, eden))
        assertFalse(OutputPlan.setupsCollide(eden, nightly))
        assertFalse(OutputPlan.setupsCollide(gameCube, wii))
        assertFalse(OutputPlan.setupsCollide(EmulatorTarget.OTHER, EmulatorTarget.OTHER))
    }

    @Test
    fun `Motion rumbles through the pad on Dolphin Wii and Eden, unless every player has it off`() {
        assertTrue(OutputPlan.motionRumblesThroughPad(Console.WIIMOTE_NUNCHUK, listOf(0, 20)))
        assertTrue(OutputPlan.motionRumblesThroughPad(Console.SWITCH_PRO, listOf(20)))
        assertFalse(OutputPlan.motionRumblesThroughPad(Console.SWITCH_PRO, listOf(0, 0)))
        assertFalse(OutputPlan.motionRumblesThroughPad(Console.WIIMOTE_NUNCHUK, emptyList()))
    }

    @Test
    fun `Motion on another emulator rumbles nothing`() {
        assertFalse(OutputPlan.motionRumblesThroughPad(Console.ANDROID, listOf(20)))
    }
}
