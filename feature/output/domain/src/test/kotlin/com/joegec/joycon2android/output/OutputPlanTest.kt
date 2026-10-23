package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.output.OutputDevice.KEYBOARD
import com.joegec.joycon2android.output.OutputDevice.MOTION
import com.joegec.joycon2android.output.OutputDevice.MOUSE
import com.joegec.joycon2android.output.OutputDevice.PAD
import org.junit.Assert.assertEquals
import org.junit.Test

class OutputPlanTest {

    @Test
    fun `each tab runs the device it is named for`() {
        assertEquals(setOf(PAD), OutputPlan.devices(OutputMode.GAMEPAD, emptySet()))
        assertEquals(setOf(MOTION), OutputPlan.devices(OutputMode.MOTION, emptySet()))
        assertEquals(setOf(KEYBOARD, MOUSE), OutputPlan.devices(OutputMode.MOUSE_KEYBOARD, emptySet()))
    }

    @Test
    fun `a mapping that presses keys or clicks brings that device up too`() {
        assertEquals(setOf(MOTION, KEYBOARD), OutputPlan.devices(OutputMode.MOTION, setOf(KEYBOARD)))
        assertEquals(setOf(PAD, MOUSE), OutputPlan.devices(OutputMode.GAMEPAD, setOf(MOUSE)))
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
}
