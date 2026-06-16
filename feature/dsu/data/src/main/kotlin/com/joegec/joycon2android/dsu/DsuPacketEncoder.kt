package com.joegec.joycon2android.dsu
import com.joegec.joycon2android.dsu.motion.DsuMotion
import com.joegec.joycon2android.dsu.motion.MotionConverter

import com.joegec.joycon2android.model.BatteryGauge
import com.joegec.joycon2android.model.GamepadState
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

/**
 * Encodes DSU (cemuhook) server packets. Spec: https://v1993.github.io/cemuhook-protocol/
 *
 * All fields little-endian. 16-byte header: magic "DSUS", uint16 protocol version (1001),
 * uint16 payload length (counts the uint32 message type that follows), uint32 CRC32
 * (computed over the whole packet with this field zeroed), uint32 server ID.
 */
class DsuPacketEncoder(
    private val serverId: Int,
    private val motion: (PlayerState) -> DsuMotion = { MotionConverter.convert(it.motionSource?.input) },
) {

    fun versionResponse(): ByteArray {
        val packet = newPacket(VERSION_RESPONSE_SIZE, TYPE_VERSION)
        packet.putShort(PROTOCOL_VERSION.toShort())
        return seal(packet)
    }

    fun portInfoResponse(slot: Int, player: PlayerState?): ByteArray {
        val packet = newPacket(PORT_INFO_RESPONSE_SIZE, TYPE_PORT_INFO)
        putControllerHeader(packet, slot, player)
        packet.put(0)
        return seal(packet)
    }

    /** Fills [buffer] (size [PAD_DATA_PACKET_SIZE]) so the 120 Hz path allocates nothing. */
    fun padData(buffer: ByteArray, state: PlayerState, packetNumber: Long, motionTimestampMicros: Long): ByteArray {
        buffer.fill(0)
        val packet = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
        putHeader(packet, PAD_DATA_PACKET_SIZE, TYPE_PAD_DATA)
        putControllerHeader(packet, state.player.index - 1, state)
        packet.put(1)
        packet.putInt(packetNumber.toInt())
        putButtons(packet, state.gamepad)
        putSticks(packet, state.gamepad)
        putAnalogButtons(packet, state.gamepad)
        packet.position(packet.position() + TOUCH_BYTES)
        putMotion(packet, state, motionTimestampMicros)
        return seal(packet)
    }

    private fun newPacket(size: Int, messageType: Int): ByteBuffer {
        val packet = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
        putHeader(packet, size, messageType)
        return packet
    }

    private fun putHeader(packet: ByteBuffer, packetSize: Int, messageType: Int) {
        packet.put(SERVER_MAGIC)
        packet.putShort(PROTOCOL_VERSION.toShort())
        packet.putShort((packetSize - HEADER_SIZE).toShort())
        packet.putInt(0) // CRC32, written by seal()
        packet.putInt(serverId)
        packet.putInt(messageType)
    }

    private fun seal(packet: ByteBuffer): ByteArray {
        val bytes = packet.array()
        val crc = CRC32().apply { update(bytes) }.value
        packet.putInt(CRC_OFFSET, crc.toInt())
        return bytes
    }

    // Slot, state, model, connection type, MAC, battery — shared by port info and pad data
    private fun putControllerHeader(packet: ByteBuffer, slot: Int, player: PlayerState?) {
        val connected = player?.hasController == true
        packet.put(slot.toByte())
        packet.put(if (connected) SLOT_STATE_CONNECTED else 0)
        packet.put(if (connected) MODEL_FULL_GYRO else 0)
        packet.put(if (connected) CONNECTION_BLUETOOTH else 0)
        packet.put(macBytes(player))
        packet.put(batteryByte(player))
    }

    private fun macBytes(player: PlayerState?): ByteArray {
        val parts = player?.motionSource?.address?.split(":") ?: return ByteArray(MAC_SIZE)
        if (parts.size != MAC_SIZE) return ByteArray(MAC_SIZE)
        return ByteArray(MAC_SIZE) { (parts[it].toIntOrNull(16) ?: 0).toByte() }
    }

    private fun batteryByte(player: PlayerState?): Byte {
        val volts = player?.motionSource?.input?.batteryVolts ?: 0f
        if (volts <= 0f) return BATTERY_NA
        val percent = BatteryGauge.percentFromVolts(volts)
        return when {
            percent >= 90 -> BATTERY_FULL
            percent >= 70 -> BATTERY_HIGH
            percent >= 40 -> BATTERY_MEDIUM
            percent >= 15 -> BATTERY_LOW
            else -> BATTERY_DYING
        }
    }

    private fun putButtons(packet: ByteBuffer, gamepad: GamepadState) {
        packet.put(bitmask(gamepad.pressed, BUTTON_MASKS_1))
        packet.put(bitmask(gamepad.pressed, BUTTON_MASKS_2))
        packet.put(if (JoyconButton.Home.id in gamepad.pressed) 1 else 0)
        // Capture stands in for the DS4 touchpad click — no other DSU slot fits it
        packet.put(if (JoyconButton.Camera.id in gamepad.pressed) 1 else 0)
    }

    private fun bitmask(pressed: Set<String>, masks: Map<String, Int>): Byte {
        var bits = 0
        for (id in pressed) bits = bits or (masks[id] ?: 0)
        return bits.toByte()
    }

    // DSU sticks are uint8 centered at 128, Y up-positive — same polarity as the raw
    // Joy-Con 0–4095 range, so scale only (unlike the HID report, which inverts Y)
    private fun putSticks(packet: ByteBuffer, gamepad: GamepadState) {
        packet.put(stickByte(gamepad.leftStickX))
        packet.put(stickByte(gamepad.leftStickY))
        packet.put(stickByte(gamepad.rightStickX))
        packet.put(stickByte(gamepad.rightStickY))
    }

    private fun stickByte(value: Int): Byte = (value / 16).coerceIn(0, 255).toByte()

    private fun putAnalogButtons(packet: ByteBuffer, gamepad: GamepadState) {
        for (button in ANALOG_BUTTON_ORDER) {
            packet.put(if (button.id in gamepad.pressed) 0xFF.toByte() else 0)
        }
    }

    private fun putMotion(packet: ByteBuffer, state: PlayerState, timestampMicros: Long) {
        val sample = motion(state)
        packet.putLong(timestampMicros)
        packet.putFloat(sample.accelX)
        packet.putFloat(sample.accelY)
        packet.putFloat(sample.accelZ)
        packet.putFloat(sample.gyroPitch)
        packet.putFloat(sample.gyroYaw)
        packet.putFloat(sample.gyroRoll)
    }

    companion object {
        const val PROTOCOL_VERSION = 1001
        const val PAD_DATA_PACKET_SIZE = 100
        const val SLOT_COUNT = 4

        private const val HEADER_SIZE = 16
        private const val CRC_OFFSET = 8
        private const val VERSION_RESPONSE_SIZE = HEADER_SIZE + 6
        private const val PORT_INFO_RESPONSE_SIZE = HEADER_SIZE + 16
        private const val MAC_SIZE = 6
        private const val TOUCH_BYTES = 12 // two untouched 6-byte touch slots

        private val SERVER_MAGIC = "DSUS".toByteArray(Charsets.US_ASCII)
        private const val TYPE_VERSION = 0x100000
        private const val TYPE_PORT_INFO = 0x100001
        private const val TYPE_PAD_DATA = 0x100002

        private const val SLOT_STATE_CONNECTED: Byte = 2
        private const val MODEL_FULL_GYRO: Byte = 2
        private const val CONNECTION_BLUETOOTH: Byte = 2

        private const val BATTERY_NA: Byte = 0x00
        private const val BATTERY_DYING: Byte = 0x01
        private const val BATTERY_LOW: Byte = 0x02
        private const val BATTERY_MEDIUM: Byte = 0x03
        private const val BATTERY_HIGH: Byte = 0x04
        private const val BATTERY_FULL: Byte = 0x05

        // Bit assignments per spec: D-Pad Left 0x80 … Share 0x01, then Y 0x80 … L2 0x01
        private val BUTTON_MASKS_1 = mapOf(
            JoyconButton.Left.id to 0x80, JoyconButton.Down.id to 0x40,
            JoyconButton.Right.id to 0x20, JoyconButton.Up.id to 0x10,
            JoyconButton.Plus.id to 0x08, JoyconButton.RS.id to 0x04,
            JoyconButton.LS.id to 0x02, JoyconButton.Minus.id to 0x01,
        )
        private val BUTTON_MASKS_2 = mapOf(
            JoyconButton.Y.id to 0x80, JoyconButton.B.id to 0x40,
            JoyconButton.A.id to 0x20, JoyconButton.X.id to 0x10,
            JoyconButton.R.id to 0x08, JoyconButton.L.id to 0x04,
            JoyconButton.ZR.id to 0x02, JoyconButton.ZL.id to 0x01,
        )

        // Analog bytes follow the bitmask order: D-Pad L/D/R/U, Y/B/A/X, R1/L1/R2/L2
        private val ANALOG_BUTTON_ORDER = listOf(
            JoyconButton.Left, JoyconButton.Down, JoyconButton.Right, JoyconButton.Up,
            JoyconButton.Y, JoyconButton.B, JoyconButton.A, JoyconButton.X,
            JoyconButton.R, JoyconButton.L, JoyconButton.ZR, JoyconButton.ZL,
        )
    }
}
