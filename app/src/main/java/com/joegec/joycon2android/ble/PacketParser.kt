package com.joegec.joycon2android.ble

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side
import java.nio.ByteBuffer
import java.nio.ByteOrder

object PacketParser {

    private const val MIN_PACKET_SIZE = 0x3B

    // Button bitmask → enum (uint32 at packet offset 0x03, little-endian)
    private val buttonMasks: List<Pair<Long, JoyconButton>> = listOf(
        0x80000000L to JoyconButton.ZL, 0x40000000L to JoyconButton.L, 0x00010000L to JoyconButton.Minus,
        0x00080000L to JoyconButton.LS, 0x01000000L to JoyconButton.Down, 0x02000000L to JoyconButton.Up,
        0x04000000L to JoyconButton.Right, 0x08000000L to JoyconButton.Left, 0x00200000L to JoyconButton.Camera,
        0x10000000L to JoyconButton.SrLeft, 0x20000000L to JoyconButton.SlLeft, 0x00100000L to JoyconButton.Home,
        0x00400000L to JoyconButton.Chat, 0x00020000L to JoyconButton.Plus, 0x00001000L to JoyconButton.SrRight,
        0x00002000L to JoyconButton.SlRight, 0x00004000L to JoyconButton.R, 0x00008000L to JoyconButton.ZR,
        0x00040000L to JoyconButton.RS, 0x00000100L to JoyconButton.Y, 0x00000200L to JoyconButton.X,
        0x00000400L to JoyconButton.B, 0x00000800L to JoyconButton.A,
    )

    fun parse(data: ByteArray, side: Side): JoyconInput? {
        if (data.size < MIN_PACKET_SIZE) return null
        val bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        val buttons = bb.getInt(0x03).toLong() and 0xFFFFFFFFL

        // Left Joy-Con uses offset 0x0A, Right uses 0x0D
        val stickOffset = when (side) {
            Side.LEFT -> 0x0A
            Side.RIGHT -> 0x0D
            else -> 0x0A // Pro/Unknown: default to left
        }
        val (sx, sy) = decodeStick(data, stickOffset)

        return JoyconInput(
            packetId = decodeUint24(data, 0),
            buttons = buttons,
            pressed = decodeButtons(buttons),
            stickX = sx,
            stickY = sy,
            accelX = bb.getShort(0x30).toInt(),
            accelY = bb.getShort(0x32).toInt(),
            accelZ = bb.getShort(0x34).toInt(),
            gyroX = bb.getShort(0x36).toInt(),
            gyroY = bb.getShort(0x38).toInt(),
            gyroZ = bb.getShort(0x3A).toInt(),
            batteryVolts = (bb.getShort(0x1F).toInt() and 0xFFFF) / 1000f,
        )
    }

    /** 12-bit packed stick: 3 bytes → (x, y) each 0..4095 */
    private fun decodeStick(data: ByteArray, offset: Int): Pair<Int, Int> {
        val v = (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8) or
            ((data[offset + 2].toInt() and 0xFF) shl 16)
        return (v and 0xFFF) to ((v shr 12) and 0xFFF)
    }

    private fun decodeUint24(data: ByteArray, offset: Int): Int =
        (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8) or
            ((data[offset + 2].toInt() and 0xFF) shl 16)

    private fun decodeButtons(buttons: Long): Set<String> =
        buttonMasks.filter { (mask, _) -> buttons and mask != 0L }.map { it.second.id }.toSet()
}
