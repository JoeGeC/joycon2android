package com.joegec.joycon2android.connection

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VibrationPlaybackTest {

    @Test
    fun `a buzz rumbles faintly frame by frame, then stops`() {
        val steps = VibrationPlayback.steps(VibrationSample.BUZZ).map { it as VibrationPlayback.Rumble }

        assertTrue(steps.dropLast(1).all { it.amplitude in 1..HdRumble.MAX_AMPLITUDE / 4 && it.gapMs == 12L })
        assertEquals(0, steps.last().amplitude)
    }

    @Test
    fun `a click is the one stored sample`() {
        val step = VibrationPlayback.steps(VibrationSample.CLICK).single() as VibrationPlayback.Sample

        assertArrayEquals(ControllerCommands.vibrationSample(0x03), step.command)
    }
}
