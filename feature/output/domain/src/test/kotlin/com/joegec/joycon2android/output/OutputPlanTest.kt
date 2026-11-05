package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.output.OutputDevice.KEYBOARD
import com.joegec.joycon2android.output.OutputDevice.MOTION
import com.joegec.joycon2android.output.OutputDevice.MOUSE
import com.joegec.joycon2android.output.OutputDevice.PAD
import org.junit.Assert.assertEquals
import org.junit.Test

class OutputPlanTest {

    private fun devices(playing: Playing, motion: Boolean = true, shizuku: Boolean = true, mapped: Set<OutputDevice> = emptySet()) =
        OutputPlan.devices(playing, motion, shizuku, mapped)

    @Test
    fun `each game runs what it reads, plus the pad for getting round the device`() {
        assertEquals(setOf(PAD, MOTION), devices(Playing.SWITCH))
        assertEquals(setOf(PAD), devices(Playing.GAMECUBE))
        assertEquals(setOf(PAD, MOTION), devices(Playing.WII))
        assertEquals(setOf(PAD, MOTION), devices(Playing.OTHER_EMULATOR))
        assertEquals(setOf(PAD), devices(Playing.ANDROID))
        assertEquals(setOf(PAD, KEYBOARD, MOUSE), devices(Playing.PC))
    }

    @Test
    fun `Switch can play without motion, the others ignore the choice`() {
        assertEquals(setOf(PAD), devices(Playing.SWITCH, motion = false))
        assertEquals(setOf(PAD, MOTION), devices(Playing.WII, motion = false))
    }

    @Test
    fun `a mapping that presses keys or clicks brings that device up too`() {
        assertEquals(setOf(PAD, MOTION, MOUSE), devices(Playing.OTHER_EMULATOR, mapped = setOf(MOUSE)))
        assertEquals(setOf(PAD, KEYBOARD), devices(Playing.ANDROID, mapped = setOf(KEYBOARD)))
    }

    @Test
    fun `without Shizuku a game that reads buttons over DSU falls back to it, and the rest get nothing`() {
        assertEquals(setOf(MOTION), devices(Playing.SWITCH, motion = false, shizuku = false))
        assertEquals(setOf(MOTION), devices(Playing.WII, shizuku = false))
        assertEquals(setOf(MOTION), devices(Playing.OTHER_EMULATOR, shizuku = false, mapped = setOf(MOUSE)))
        assertEquals(emptySet<OutputDevice>(), devices(Playing.GAMECUBE, shizuku = false))
        assertEquals(emptySet<OutputDevice>(), devices(Playing.PC, shizuku = false))
    }

    @Test
    fun `each game maps its own console, with a set-up emulator where there is one`() {
        assertEquals(EmulatorKind.EDEN.console, Playing.SWITCH.console)
        assertEquals(EmulatorKind.DOLPHIN_WII.console, Playing.WII.console)
        assertEquals(null, Playing.OTHER_EMULATOR.emulator)
        assertEquals(setOf(Playing.ANDROID), Playing.entries.filter { it.console == Console.ANDROID }.toSet())
    }
}
