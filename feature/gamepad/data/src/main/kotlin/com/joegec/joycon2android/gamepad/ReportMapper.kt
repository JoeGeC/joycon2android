package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.JoyconButton

object ReportMapper {

    private const val REPORT_SIZE = 15

    // Each bit's keycode carries the button's own name: docs/virtual-gamepad.md#buttons-and-keycodes
    private val BUTTON_MAP: Map<String, Int> = mapOf(
        JoyconButton.A.id to 0,
        JoyconButton.B.id to 1,
        JoyconButton.Capture.id to 2,
        JoyconButton.X.id to 3,
        JoyconButton.Y.id to 4,
        JoyconButton.GL.id to 5,
        JoyconButton.L.id to 6,
        JoyconButton.R.id to 7,
        JoyconButton.ZL.id to 8,
        JoyconButton.ZR.id to 9,
        JoyconButton.Minus.id to 10,
        JoyconButton.Plus.id to 11,
        JoyconButton.Home.id to 12,
        JoyconButton.LS.id to 13,
        JoyconButton.RS.id to 14,
    )

    // The two that overflow the gamepad collection, in its trailing vendor collection's byte.
    private val OVERFLOW_MAP: Map<String, Int> = mapOf(
        JoyconButton.GR.id to 0,
        JoyconButton.Chat.id to 1,
    )

    // The trailing Consumer collection's byte: docs/virtual-gamepad.md#android-keys
    internal val ANDROID_KEY_BITS: Map<AndroidKey, Int> = mapOf(
        AndroidKey.HOME to 0,
        AndroidKey.BACK to 1,
    )

    private const val HAT_CENTER = 0x0F

    fun buildReport(frame: GamepadFrame): ByteArray {
        val gamepad = frame.state.gamepad
        val report = ByteArray(REPORT_SIZE)
        val pressed = gamepad.pressed

        // Bytes 0-1: 15 button bits (little-endian) then one padding bit
        val buttons = bits(pressed, BUTTON_MAP)
        report[0] = (buttons and 0xFF).toByte()
        report[1] = ((buttons shr 8) and 0xFF).toByte()

        // Byte 2: hat switch in lower nibble, upper nibble is padding (zeros)
        report[2] = hatFromPressed(pressed).toByte()

        // Bytes 3-4: left stick X (16-bit signed LE)
        putInt16LE(report, 3, mapStick(gamepad.leftStickX))
        // Bytes 5-6: left stick Y (16-bit signed LE, inverted for HID convention)
        putInt16LE(report, 5, mapStick(4096 - gamepad.leftStickY))
        // Bytes 7-8: right stick X
        putInt16LE(report, 7, mapStick(gamepad.rightStickX))
        // Bytes 9-10: right stick Y (inverted)
        putInt16LE(report, 9, mapStick(4096 - gamepad.rightStickY))

        report[11] = gamepad.leftTrigger.toByte()
        report[12] = gamepad.rightTrigger.toByte()

        report[13] = bits(pressed, OVERFLOW_MAP).toByte()

        report[14] = frame.androidKeys
            .mapNotNull(ANDROID_KEY_BITS::get)
            .fold(0) { acc, bit -> acc or (1 shl bit) }
            .toByte()

        return report
    }

    private fun bits(pressed: Set<String>, positions: Map<String, Int>): Int =
        pressed.fold(0) { acc, name -> positions[name]?.let { acc or (1 shl it) } ?: acc }

    // Map 0-4095 (center 2048) → -32767..32767
    private fun mapStick(value: Int): Int {
        return ((value - 2048).toLong() * 32767 / 2048).toInt().coerceIn(-32767, 32767)
    }

    private fun putInt16LE(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }

    // Hat switch: 0=N, 1=NE, 2=E, 3=SE, 4=S, 5=SW, 6=W, 7=NW, 0x0F=center
    private fun hatFromPressed(pressed: Set<String>): Int {
        val up = JoyconButton.Up.id in pressed
        val down = JoyconButton.Down.id in pressed
        val left = JoyconButton.Left.id in pressed
        val right = JoyconButton.Right.id in pressed
        return when {
            up && right -> 1
            right && down -> 3
            down && left -> 5
            left && up -> 7
            up -> 0
            right -> 2
            down -> 4
            left -> 6
            else -> HAT_CENTER
        }
    }
}
