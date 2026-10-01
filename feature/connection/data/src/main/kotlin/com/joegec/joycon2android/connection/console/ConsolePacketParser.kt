package com.joegec.joycon2android.connection.console

import com.joegec.joycon2android.connection.PacketParser
import com.joegec.joycon2android.model.BatteryCharge
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.MotionSupport
import com.joegec.joycon2android.model.Side

/** Console input report, translated into the common bitmask: docs/protocol.md#input-report */
internal object ConsolePacketParser {

    private const val MIN_REPORT_SIZE = 8
    private const val MAX_BATTERY_LEVEL = 9

    // Motion block: docs/protocol.md#motion-block
    private const val MOTION_LEFT = 0x0F
    private const val MOTION_RIGHT = 0x10
    private const val ACCEL_IN_BLOCK = 0x12
    private const val BLOCK_WITH_ACCEL = 0x1C
    private const val AXIS_STRIDE = 4
    private const val AXES = 3
    private val NO_AXES = IntArray(AXES)

    private val rightButtons = listOf(
        0 to 0x00000400L, // B
        1 to 0x00000800L, // A
        2 to 0x00000100L, // Y
        3 to 0x00000200L, // X
        4 to 0x00004000L, // R
        5 to 0x00008000L, // ZR
        6 to 0x00020000L, // +
        7 to 0x00040000L, // RS
        8 to 0x00100000L, // Home
        12 to 0x00400000L, // C
        14 to 0x00001000L, // SR
        15 to 0x00002000L, // SL
    )

    private val leftButtons = listOf(
        0 to 0x01000000L, // Down
        1 to 0x04000000L, // Right
        2 to 0x08000000L, // Left
        3 to 0x02000000L, // Up
        4 to 0x40000000L, // L
        5 to 0x80000000L, // ZL
        6 to 0x00010000L, // -
        7 to 0x00080000L, // LS
        8 to 0x00200000L, // Capture
        14 to 0x10000000L, // SR
        15 to 0x20000000L, // SL
    )

    fun counter(report: ByteArray): Int = report[0].toInt() and 0xFF

    fun parse(report: ByteArray, side: Side, packetId: Int): JoyconInput? {
        if (report.size < MIN_REPORT_SIZE) return null
        val raw = u8(report, 2) or (u8(report, 3) shl 8)
        val table = if (side == Side.LEFT) leftButtons else rightButtons
        val buttons = table.fold(0L) { acc, (bit, mask) -> if (raw shr bit and 1 == 1) acc or mask else acc }
        val stick = u8(report, 5) or (u8(report, 6) shl 8) or (u8(report, 7) shl 16)
        val level = (u8(report, 1) shr 2 and 0x0F).coerceAtMost(MAX_BATTERY_LEVEL)
        val accel = accel(report, side)
        val axes = accel ?: NO_AXES

        return JoyconInput(
            packetId = packetId,
            buttons = buttons,
            pressed = PacketParser.decodeButtons(buttons),
            stickX = stick and 0xFFF,
            stickY = stick shr 12 and 0xFFF,
            battery = BatteryCharge.fromLevel(level, MAX_BATTERY_LEVEL),
            accelX = axes[0],
            accelY = -axes[1],
            accelZ = axes[2],
            motionSupport = if (accel == null) MotionSupport.None else MotionSupport.AccelerometerOnly,
        )
    }

    // Each axis is an int16 in the high half of its word; y runs opposite a genuine Joy-Con 2.
    // Null for a block too short to hold them, or one whose words are not shaped that way at all.
    private fun accel(report: ByteArray, side: Side): IntArray? {
        val block = if (side == Side.LEFT) MOTION_LEFT else MOTION_RIGHT
        val first = block + ACCEL_IN_BLOCK
        if (report.size < first + (AXES - 1) * AXIS_STRIDE + 2) return null
        if (u8(report, block - 1) < BLOCK_WITH_ACCEL) return null
        if ((0 until AXES).any { axis -> lowHalf(report, first + axis * AXIS_STRIDE) != 0 }) return null
        return IntArray(AXES) { axis -> i16(report, first + axis * AXIS_STRIDE) }
    }

    private fun lowHalf(report: ByteArray, axis: Int) = u8(report, axis - 2) or u8(report, axis - 1)

    private fun i16(data: ByteArray, index: Int): Int {
        val value = u8(data, index) or (u8(data, index + 1) shl 8)
        return if (value and 0x8000 != 0) value - 0x10000 else value
    }

    private fun u8(data: ByteArray, index: Int) = data[index].toInt() and 0xFF
}
