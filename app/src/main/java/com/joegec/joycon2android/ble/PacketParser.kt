package com.joegec.joycon2android.ble

import com.joegec.joycon2android.model.Joycon2State
import com.joegec.joycon2android.model.Side
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Parses the 63-byte BLE notification packets from Joy-Con 2.
 * All offsets and formats are from the working macOS implementation
 * (see joycon2_android_reference.md).
 */
object PacketParser {

    private const val MIN_PACKET_SIZE = 0x3E // need through offset 0x3D for triggers

    // Button bitmask → name (uint32 at packet offset 0x03, little-endian)
    private val buttonMasks: List<Pair<Long, String>> = listOf(
        0x80000000L to "ZL", 0x40000000L to "L", 0x00010000L to "-",
        0x00080000L to "LS", 0x01000000L to "Down", 0x02000000L to "Up",
        0x04000000L to "Right", 0x08000000L to "Left", 0x00200000L to "Camera",
        0x10000000L to "SR(L)", 0x20000000L to "SL(L)", 0x00100000L to "Home",
        0x00400000L to "Chat", 0x00020000L to "+", 0x00001000L to "SR(R)",
        0x00002000L to "SL(R)", 0x00004000L to "R", 0x00008000L to "ZR",
        0x00040000L to "RS", 0x00000100L to "Y", 0x00000200L to "X",
        0x00000400L to "B", 0x00000800L to "A",
    )

    fun parse(data: ByteArray, side: Side): Joycon2State? {
        if (data.size < MIN_PACKET_SIZE) return null
        val bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        val buttons = bb.getInt(0x03).toLong() and 0xFFFFFFFFL
        val (lx, ly) = decodeStick(data, 0x0A)
        val (rx, ry) = decodeStick(data, 0x0D)

        return Joycon2State(
            connected = true,
            side = side,
            packetId = decodeUint24(data, 0),
            buttons = buttons,
            pressed = decodeButtons(buttons),
            leftStickX = lx, leftStickY = ly,
            rightStickX = rx, rightStickY = ry,
            triggerL = data[0x3C].toInt() and 0xFF,
            triggerR = data[0x3D].toInt() and 0xFF,
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
        buttonMasks.filter { (mask, _) -> buttons and mask != 0L }.map { it.second }.toSet()
}
