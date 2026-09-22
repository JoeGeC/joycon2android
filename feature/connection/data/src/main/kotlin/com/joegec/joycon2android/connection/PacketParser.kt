package com.joegec.joycon2android.connection


import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side
import java.nio.ByteBuffer
import java.nio.ByteOrder

object PacketParser {

    private const val MIN_PACKET_SIZE = 0x3B

    // Button bitmask → enum. Bits 0..31 come from the uint32 at packet offset 0x03; the Pro
    // Controller's two back paddles live in the next byte (0x07), folded into bits 32..39 so the
    // whole set decodes through one mask table. GR is bit 0 of byte 0x07, GL is bit 1.
    private val buttonMasks: List<Pair<Long, JoyconButton>> = listOf(
        0x80000000L to JoyconButton.ZL, 0x40000000L to JoyconButton.L, 0x00010000L to JoyconButton.Minus,
        0x00080000L to JoyconButton.LS, 0x01000000L to JoyconButton.Down, 0x02000000L to JoyconButton.Up,
        0x04000000L to JoyconButton.Right, 0x08000000L to JoyconButton.Left, 0x00200000L to JoyconButton.Capture,
        0x10000000L to JoyconButton.SrLeft, 0x20000000L to JoyconButton.SlLeft, 0x00100000L to JoyconButton.Home,
        0x00400000L to JoyconButton.Chat, 0x00020000L to JoyconButton.Plus, 0x00001000L to JoyconButton.SrRight,
        0x00002000L to JoyconButton.SlRight, 0x00004000L to JoyconButton.R, 0x00008000L to JoyconButton.ZR,
        0x00040000L to JoyconButton.RS, 0x00000100L to JoyconButton.Y, 0x00000200L to JoyconButton.X,
        0x00000400L to JoyconButton.B, 0x00000800L to JoyconButton.A,
        0x0100000000L to JoyconButton.GR, 0x0200000000L to JoyconButton.GL,
    )

    fun parse(data: ByteArray, side: Side, isNyxiChar: Boolean = false): JoyconInput? {
        if (data.size < 12) return null

        if (isNyxiChar || isNyxiFormat(data)) {
            return parseNyxiFormat(data, side)
        }

        // Standard Switch parser should only run on non-Nyxi characteristics
        if (data.size < MIN_PACKET_SIZE) return null
        val bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        val buttons = (bb.getInt(0x03).toLong() and 0xFFFFFFFFL) or
            ((data[0x07].toInt() and 0xFF).toLong() shl 32)
        val (sx, sy) = resolveStick(data, side)
        val (rsx, rsy) = if (side == Side.PRO) decodeStick(data, 0x0D) else (2048 to 2048)

        return JoyconInput(
            packetId = decodeUint24(data, 0),
            buttons = buttons,
            pressed = decodeButtons(buttons),
            stickX = sx,
            stickY = sy,
            rightStickX = rsx,
            rightStickY = rsy,
            accelX = bb.getShort(0x30).toInt(),
            accelY = bb.getShort(0x32).toInt(),
            accelZ = bb.getShort(0x34).toInt(),
            gyroX = bb.getShort(0x36).toInt(),
            gyroY = bb.getShort(0x38).toInt(),
            gyroZ = bb.getShort(0x3A).toInt(),
            batteryVolts = (bb.getShort(0x1F).toInt() and 0xFFFF) / 1000f,
        )
    }

    private fun isNyxiFormat(data: ByteArray): Boolean {
        if (data.size < 8) return false
        val status = data[1].toInt() and 0xFF
        
        // Accept any 0x0X or 0x1X status as potential Nyxi input
        val isInputStatus = (status in 0x00..0x1F) || 
                            status == 0x80 || status == 0x81 || status == 0x8E || status == 0x3F
        
        return isInputStatus
    }

    private fun parseNyxiFormat(data: ByteArray, side: Side): JoyconInput? {
        val status = data[1].toInt() and 0xFF
        
        // Accept any low-range status (0x0X, 0x1X) as input, provided it's not a generic vendor header (0xFE).
        // Some Nyxi controllers swap status codes (e.g. 0x10 for Left, 0x0C for Right) or use new ones like 0x1C.
        val isValidStatus = (status in 0x00..0x1F) || 
                            status == 0x80 || status == 0x81 || status == 0x8E || status == 0x3F
        
        if (!isValidStatus || (data[0].toInt() and 0xFF == 0xFE)) {
            return null
        }



        val b2 = data[2].toInt() and 0xFF
        val b3 = data[3].toInt() and 0xFF
        val b4 = data[4].toInt() and 0xFF

        val pressed = mutableSetOf<String>()


        // Byte 2 & Byte 3: Button mappings depend on whether this is the Left or Right controller half
        if (side == Side.LEFT) {
            // Left Joy-Con Byte 2 (D-Pad & Left shoulders - oriented for sideways single Joy-Con display)
            if ((b2 and 0x01) != 0) pressed.add(JoyconButton.Down.id)   // Physical Right -> Screen ▶
            if ((b2 and 0x02) != 0) pressed.add(JoyconButton.Right.id)  // Physical Up -> Screen ▲
            if ((b2 and 0x04) != 0) pressed.add(JoyconButton.Left.id)   // Physical Down -> Screen ▼
            if ((b2 and 0x08) != 0) pressed.add(JoyconButton.Up.id)     // Physical Left -> Screen ◀
            if ((b2 and 0x10) != 0) pressed.add(JoyconButton.L.id)
            if ((b2 and 0x20) != 0) pressed.add(JoyconButton.ZL.id)
            if ((b2 and 0x40) != 0) pressed.add(JoyconButton.Minus.id)
            if ((b2 and 0x80) != 0) pressed.add(JoyconButton.LS.id)

            // Left Joy-Con Byte 3 (Capture, SL/SR, etc.)
            if ((b3 and 0x01) != 0) pressed.add(JoyconButton.Capture.id) // "O" button
            if ((b3 and 0x02) != 0) pressed.add(JoyconButton.Minus.id)
            if ((b3 and 0x04) != 0) pressed.add(JoyconButton.LS.id)
            if ((b3 and 0x10) != 0) pressed.add(JoyconButton.Capture.id)
            if ((b3 and 0x20) != 0) pressed.add(JoyconButton.GL.id)
            if ((b3 and 0x40) != 0) pressed.add(JoyconButton.SrLeft.id)
            if ((b3 and 0x80) != 0) pressed.add(JoyconButton.SlLeft.id)
        } else {
            // Right / Pro Controller Byte 2 (Face buttons & Right shoulders)
            if ((b2 and 0x01) != 0) pressed.add(JoyconButton.B.id)
            if ((b2 and 0x02) != 0) pressed.add(JoyconButton.A.id)
            if ((b2 and 0x04) != 0) pressed.add(JoyconButton.Y.id)
            if ((b2 and 0x08) != 0) pressed.add(JoyconButton.X.id)
            if ((b2 and 0x10) != 0) pressed.add(JoyconButton.R.id)
            if ((b2 and 0x20) != 0) pressed.add(JoyconButton.ZR.id)
            if ((b2 and 0x40) != 0) pressed.add(JoyconButton.Plus.id)
            if ((b2 and 0x80) != 0) pressed.add(JoyconButton.RS.id)

            // Right / Pro Controller Byte 3 (Home, Chat, SL/SR, etc.)
            if ((b3 and 0x01) != 0) pressed.add(JoyconButton.Home.id)
            if ((b3 and 0x02) != 0) pressed.add(JoyconButton.Plus.id)
            if ((b3 and 0x04) != 0) pressed.add(JoyconButton.RS.id)
            if ((b3 and 0x10) != 0) pressed.add(JoyconButton.Chat.id)   // "C" Chat button on Right Joy-Con
            if ((b3 and 0x20) != 0) pressed.add(JoyconButton.GR.id)
            if ((b3 and 0x40) != 0) pressed.add(JoyconButton.SrRight.id)
            if ((b3 and 0x80) != 0) pressed.add(JoyconButton.SlRight.id)
        }

        // Byte 4 D-Pad Hat Switch
        val hat = b4 and 0x0F
        when (hat) {
            0 -> pressed.add(JoyconButton.Up.id)
            1 -> { pressed.add(JoyconButton.Up.id); pressed.add(JoyconButton.Right.id) }
            2 -> pressed.add(JoyconButton.Right.id)
            3 -> { pressed.add(JoyconButton.Down.id); pressed.add(JoyconButton.Right.id) }
            4 -> pressed.add(JoyconButton.Down.id)
            5 -> { pressed.add(JoyconButton.Down.id); pressed.add(JoyconButton.Left.id) }
            6 -> pressed.add(JoyconButton.Left.id)
        }

        // Stick decoding: Primary stick is usually at offset 5. 
        // Second stick (Pro) at offset 8. Some Right Joy-Cons use offset 8 exclusively.
        val (s1x, s1y) = if (data.size >= 8) decodeStick(data, 5) else (2048 to 2048)
        val (s2x, s2y) = if (data.size >= 11) decodeStick(data, 8) else (2048 to 2048)

        val s1Active = isStickActive(s1x to s1y)
        val s2Active = isStickActive(s2x to s2y)
        
        val primaryX = when {
            side == Side.LEFT -> s1x
            side == Side.RIGHT -> if (s2Active && !s1Active) s2x else s1x
            else -> s1x
        }
        val primaryY = when {
            side == Side.LEFT -> s1y
            side == Side.RIGHT -> if (s2Active && !s1Active) s2y else s1y
            else -> s1y
        }

        return JoyconInput(
            packetId = (data[0].toInt() and 0xFF),
            pressed = pressed,
            stickX = primaryX,
            stickY = primaryY,
            rightStickX = if (side == Side.PRO || side == Side.RIGHT) s2x else 2048,
            rightStickY = if (side == Side.PRO || side == Side.RIGHT) s2y else 2048,
        )
    }

    private fun resolveStick(data: ByteArray, side: Side): Pair<Int, Int> {
        if (side == Side.LEFT || side == Side.PRO) return decodeStick(data, 0x0A)
        if (side == Side.RIGHT) return decodeStick(data, 0x0D)

        // For UNKNOWN: check both offsets and use whichever has non-center data
        val left = decodeStick(data, 0x0A)
        val right = decodeStick(data, 0x0D)
        val leftActive = isStickActive(left)
        val rightActive = isStickActive(right)
        return when {
            leftActive && !rightActive -> left
            rightActive && !leftActive -> right
            leftActive -> left
            else -> left
        }
    }

    private fun isStickActive(stick: Pair<Int, Int>): Boolean {
        val (x, y) = stick
        return (x != 0 || y != 0) && (x != 2048 || y != 2048)
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
