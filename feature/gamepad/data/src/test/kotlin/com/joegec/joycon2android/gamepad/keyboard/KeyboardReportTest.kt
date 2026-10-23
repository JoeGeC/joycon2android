package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.KeyboardKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardReportTest {

    @Test
    fun `nothing held releases every key`() {
        val (keys, consumer) = KeyboardReport.build(KeyboardFrame())

        assertArrayEquals(byteArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0), keys)
        assertArrayEquals(byteArrayOf(2, 0), consumer)
    }

    @Test
    fun `screenshot holds Print Screen alongside keyboard keys`() {
        val (keys, _) = KeyboardReport.build(KeyboardFrame(setOf(AndroidKey.SCREENSHOT), setOf(KeyboardKey.W)))

        assertArrayEquals(byteArrayOf(1, 0, 0, 0x46, 0x1A, 0, 0, 0, 0), keys)
    }

    @Test
    fun `home and back are the Consumer report's first two bits`() {
        val (_, consumer) = KeyboardReport.build(KeyboardFrame(setOf(AndroidKey.HOME, AndroidKey.BACK)))

        assertArrayEquals(byteArrayOf(2, 0b11), consumer)
    }

    @Test
    fun `modifiers are bits in the first byte, not slots`() {
        val (keys, _) = KeyboardReport.build(KeyboardFrame(keys = setOf(KeyboardKey.LEFT_SHIFT, KeyboardKey.LEFT_CTRL)))

        assertArrayEquals(byteArrayOf(1, 0b11, 0, 0, 0, 0, 0, 0, 0), keys)
    }

    @Test
    fun `only six keys are held at once`() {
        val held = setOf(KeyboardKey.A, KeyboardKey.B, KeyboardKey.C, KeyboardKey.D, KeyboardKey.E, KeyboardKey.F, KeyboardKey.G)

        val (keys, _) = KeyboardReport.build(KeyboardFrame(keys = held))

        assertEquals(6, keys.drop(3).count { it != 0.toByte() })
    }
}
