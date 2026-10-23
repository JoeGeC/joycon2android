package com.joegec.joycon2android.gamepad.mouse

/** Laid out by [MouseDescriptor]: buttons, X and Y as int16, then the wheel as int8. */
object MouseReport {

    private const val SIZE = 6

    private val BUTTON_BITS = mapOf(MouseButton.PRIMARY to 0, MouseButton.SECONDARY to 1, MouseButton.MIDDLE to 2)

    fun build(frame: MouseFrame): ByteArray {
        val report = ByteArray(SIZE)
        report[0] = frame.buttons.fold(0) { acc, button -> acc or (1 shl BUTTON_BITS.getValue(button)) }.toByte()
        putInt16LE(report, 1, frame.dx.coerceIn(-Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt()))
        putInt16LE(report, 3, frame.dy.coerceIn(-Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt()))
        report[5] = frame.wheel.coerceIn(-Byte.MAX_VALUE.toInt(), Byte.MAX_VALUE.toInt()).toByte()
        return report
    }

    private fun putInt16LE(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}
