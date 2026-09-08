package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the bit each button takes, because the bit *is* the Android keycode: Linux maps HID Button
 * n to BTN_GAMEPAD + n - 1 and the key layout names that. Move a button and every emulator config
 * the app writes points at the wrong control.
 */
class ReportMapperTest {

    private fun report(vararg pressed: JoyconButton): ByteArray {
        val input = JoyconInput(
            packetId = 0,
            buttons = 0,
            pressed = pressed.map { it.id }.toSet(),
            stickX = 2048,
            stickY = 2048,
            rightStickX = 2048,
            rightStickY = 2048,
            accelX = 0, accelY = 0, accelZ = 0,
            gyroX = 0, gyroY = 0, gyroZ = 0,
            batteryVolts = 4f,
        )
        val pro = ConnectedJoycon(address = "pro", side = Side.PRO, deviceName = "Pro", input = input)
        return ReportMapper.buildReport(PlayerState(PlayerNumber.P1, left = pro))
    }

    private fun bitOf(button: JoyconButton): Int {
        val r = report(button)
        val word = (r[0].toInt() and 0xFF) or ((r[1].toInt() and 0xFF) shl 8)
        return Integer.numberOfTrailingZeros(word)
    }

    @Test
    fun `each button takes the bit whose keycode carries its name`() {
        val expected = mapOf(
            JoyconButton.A to 0,        // BUTTON_A 96
            JoyconButton.B to 1,        // BUTTON_B 97
            JoyconButton.Camera to 2,   // BUTTON_C 98
            JoyconButton.X to 3,        // BUTTON_X 99
            JoyconButton.Y to 4,        // BUTTON_Y 100
            JoyconButton.GL to 5,       // BUTTON_Z 101
            JoyconButton.L to 6,        // BUTTON_L1 102
            JoyconButton.R to 7,        // BUTTON_R1 103
            JoyconButton.ZL to 8,       // BUTTON_L2 104
            JoyconButton.ZR to 9,       // BUTTON_R2 105
            JoyconButton.Minus to 10,   // BUTTON_SELECT 109
            JoyconButton.Plus to 11,    // BUTTON_START 108
            JoyconButton.Home to 12,    // BUTTON_MODE 110
            JoyconButton.LS to 13,      // BUTTON_THUMBL 106
            JoyconButton.RS to 14,      // BUTTON_THUMBR 107
        )
        expected.forEach { (button, bit) -> assertEquals(button.name, bit, bitOf(button)) }
    }

    @Test
    fun `the two buttons the gamepad collection cannot carry ride the overflow byte`() {
        assertEquals(0b01, report(JoyconButton.GR)[13].toInt())
        assertEquals(0b10, report(JoyconButton.Chat)[13].toInt())
        assertEquals(0, report(JoyconButton.GR)[1].toInt() and 0x80) // never bit 15, the pad bit
    }

    @Test
    fun `ZL drives the brake and ZR the accelerator, matching Android's trigger aliases`() {
        assertEquals(0xFF.toByte(), report(JoyconButton.ZL)[11])
        assertEquals(0x00.toByte(), report(JoyconButton.ZL)[12])
        assertEquals(0xFF.toByte(), report(JoyconButton.ZR)[12])
        assertEquals(0x00.toByte(), report(JoyconButton.ZR)[11])
    }
}
