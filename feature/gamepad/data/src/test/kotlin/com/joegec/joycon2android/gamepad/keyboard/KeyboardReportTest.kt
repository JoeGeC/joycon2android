package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.AndroidKey
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class KeyboardReportTest {

    @Test
    fun `nothing held releases every key`() {
        val (keys, consumer) = KeyboardReport.build(emptySet())

        assertArrayEquals(byteArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0), keys)
        assertArrayEquals(byteArrayOf(2, 0), consumer)
    }

    @Test
    fun `screenshot holds Print Screen`() {
        val (keys, _) = KeyboardReport.build(setOf(AndroidKey.SCREENSHOT))

        assertArrayEquals(byteArrayOf(1, 0, 0, 0x46, 0, 0, 0, 0, 0), keys)
    }

    @Test
    fun `home and back are the Consumer report's first two bits`() {
        val (_, consumer) = KeyboardReport.build(setOf(AndroidKey.HOME, AndroidKey.BACK))

        assertArrayEquals(byteArrayOf(2, 0b11), consumer)
    }
}
