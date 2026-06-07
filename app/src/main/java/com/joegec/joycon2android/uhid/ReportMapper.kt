package com.joegec.joycon2android.uhid

import com.joegec.joycon2android.model.PlayerState

object ReportMapper {

    private const val REPORT_SIZE = 13

    // Button bit positions matching the HID report descriptor order (Button 1-14)
    private val BUTTON_MAP: Map<String, Int> = mapOf(
        "A" to 0,
        "B" to 1,
        "X" to 2,
        "Y" to 3,
        "L" to 4,
        "R" to 5,
        "ZL" to 6,
        "ZR" to 7,
        "-" to 8,
        "+" to 9,
        "LS" to 10,
        "RS" to 11,
        "Home" to 12,
        "Camera" to 13,
    )

    private const val HAT_CENTER = 0x0F

    fun buildReport(state: PlayerState): ByteArray {
        val report = ByteArray(REPORT_SIZE)
        val pressed = state.pressed

        // Bytes 0-1: 14 button bits + 2 padding (little-endian)
        var buttons = 0
        for (name in pressed) {
            BUTTON_MAP[name]?.let { bit -> buttons = buttons or (1 shl bit) }
        }
        report[0] = (buttons and 0xFF).toByte()
        report[1] = ((buttons shr 8) and 0xFF).toByte()

        // Byte 2: hat switch in lower nibble, upper nibble is padding (zeros)
        report[2] = hatFromPressed(pressed).toByte()

        // Bytes 3-4: left stick X (16-bit signed LE)
        putInt16LE(report, 3, mapStick(state.leftStickX))
        // Bytes 5-6: left stick Y (16-bit signed LE, inverted for HID convention)
        putInt16LE(report, 5, mapStick(4096 - state.leftStickY))
        // Bytes 7-8: right stick X
        putInt16LE(report, 7, mapStick(state.rightStickX))
        // Bytes 9-10: right stick Y (inverted)
        putInt16LE(report, 9, mapStick(4096 - state.rightStickY))

        // Byte 11: left trigger (digital: 0 or 255)
        report[11] = if ("ZL" in pressed) 0xFF.toByte() else 0x00
        // Byte 12: right trigger (digital: 0 or 255)
        report[12] = if ("ZR" in pressed) 0xFF.toByte() else 0x00

        return report
    }

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
        val up = "Up" in pressed
        val down = "Down" in pressed
        val left = "Left" in pressed
        val right = "Right" in pressed
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
