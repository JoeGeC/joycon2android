package com.joegec.joycon2android.gamepad.mouse

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class MouseReportTest {

    @Test
    fun `buttons then X and Y little-endian then the wheel`() {
        val report = MouseReport.build(MouseFrame(dx = 300, dy = -2, wheel = -1, buttons = setOf(MouseButton.SECONDARY)))

        assertArrayEquals(byteArrayOf(0x02, 0x2C, 0x01, 0xFE.toByte(), 0xFF.toByte(), 0xFF.toByte()), report)
    }

    @Test
    fun `movement beyond the field clamps`() {
        val report = MouseReport.build(MouseFrame(dx = 40_000, wheel = 300, buttons = setOf(MouseButton.PRIMARY)))

        assertArrayEquals(byteArrayOf(0x01, 0xFF.toByte(), 0x7F, 0x00, 0x00, 0x7F), report)
    }
}
