package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.output.OutputDevice.KEYBOARD
import com.joegec.joycon2android.output.OutputDevice.MOTION
import com.joegec.joycon2android.output.OutputDevice.MOUSE
import com.joegec.joycon2android.output.OutputDevice.PAD
import org.junit.Assert.assertEquals
import org.junit.Test

class OutputPlanTest {

    private fun devices(
        mode: OutputMode,
        emulator: EmulatorKind? = null,
        mapped: Set<OutputDevice> = emptySet(),
        vararg overrides: Pair<OutputDevice, DeviceOverride>,
    ) = OutputPlan.devices(OutputSelection(mode, overrides = overrides.toMap()), emulator, mapped)

    @Test
    fun `each emulator runs only what it reads`() {
        assertEquals(setOf(PAD), devices(OutputMode.CONTROLLER, EmulatorKind.DOLPHIN_GAMECUBE))
        assertEquals(setOf(MOTION), devices(OutputMode.CONTROLLER, EmulatorKind.DOLPHIN_WII))
        assertEquals(setOf(MOTION), devices(OutputMode.CONTROLLER, EmulatorKind.EDEN))
    }

    @Test
    fun `the other targets run the device they name, and mouse and keyboard games no pad at all`() {
        assertEquals(setOf(PAD), devices(OutputMode.CONTROLLER, EmulatorKind.OTHER_GAMEPAD))
        assertEquals(setOf(MOTION), devices(OutputMode.CONTROLLER, EmulatorKind.OTHER_MOTION))
        assertEquals(setOf(KEYBOARD, MOUSE), devices(OutputMode.MOUSE_KEYBOARD, EmulatorKind.DOLPHIN_GAMECUBE))
    }

    @Test
    fun `the other targets are set up by hand`() {
        assertEquals(null, OutputPlan.setUpThrough(EmulatorKind.OTHER_GAMEPAD, setOf(PAD)))
        assertEquals(null, OutputPlan.setUpThrough(EmulatorKind.OTHER_MOTION, setOf(MOTION)))
    }

    @Test
    fun `a mapping that presses keys or clicks brings that device up in any mode`() {
        assertEquals(setOf(PAD, MOUSE), devices(OutputMode.CONTROLLER, EmulatorKind.OTHER_GAMEPAD, setOf(MOUSE)))
        assertEquals(setOf(MOTION, KEYBOARD), devices(OutputMode.CONTROLLER, EmulatorKind.EDEN, mapped = setOf(KEYBOARD)))
    }

    @Test
    fun `Off beats the mapping`() {
        assertEquals(setOf(PAD), devices(OutputMode.CONTROLLER, EmulatorKind.OTHER_GAMEPAD, setOf(MOUSE), MOUSE to DeviceOverride.OFF))
    }

    @Test
    fun `overrides beat the mode`() {
        val edenByPad = devices(OutputMode.CONTROLLER, EmulatorKind.EDEN, emptySet(), PAD to DeviceOverride.ON, MOTION to DeviceOverride.OFF)

        assertEquals(setOf(PAD), edenByPad)
    }

    @Test
    fun `Eden is set up through DSU, or the pad when only the pad runs`() {
        assertEquals(MOTION, OutputPlan.setUpThrough(EmulatorKind.EDEN, setOf(PAD, MOTION)))
        assertEquals(PAD, OutputPlan.setUpThrough(EmulatorKind.EDEN, setOf(PAD)))
        assertEquals(null, OutputPlan.setUpThrough(EmulatorKind.EDEN, setOf(KEYBOARD)))
    }

    @Test
    fun `Dolphin is set up for the console its entry names`() {
        assertEquals(PAD, OutputPlan.setUpThrough(EmulatorKind.DOLPHIN_GAMECUBE, setOf(MOTION)))
        assertEquals(MOTION, OutputPlan.setUpThrough(EmulatorKind.DOLPHIN_WII, setOf(PAD)))
    }

    @Test
    fun `each mode maps its own console`() {
        assertEquals(Console.WIIMOTE_NUNCHUK, OutputPlan.console(OutputMode.CONTROLLER, EmulatorKind.DOLPHIN_WII))
        assertEquals(Console.ANDROID, OutputPlan.console(OutputMode.CONTROLLER, EmulatorKind.OTHER_GAMEPAD))
        assertEquals(Console.PC, OutputPlan.console(OutputMode.MOUSE_KEYBOARD, null))
    }
}
