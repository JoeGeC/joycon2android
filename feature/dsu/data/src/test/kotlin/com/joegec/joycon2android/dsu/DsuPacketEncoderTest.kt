package com.joegec.joycon2android.dsu
import com.joegec.joycon2android.dsu.motion.MotionConverter

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DsuPacketEncoderTest {

    private val serverId = 0x11223344
    private val encoder = DsuPacketEncoder(serverId)

    @Test
    fun `version response matches the spec golden bytes`() {
        val packet = encoder.versionResponse()

        val expected = ByteBuffer.allocate(22).order(ByteOrder.LITTLE_ENDIAN)
            .put("DSUS".toByteArray(Charsets.US_ASCII))
            .putShort(1001)
            .putShort(6)
            .putInt(0) // CRC, checked separately
            .putInt(serverId)
            .putInt(0x100000)
            .putShort(1001)
            .array()
        assertArrayEquals(expected.copyOfRange(0, 8), packet.copyOfRange(0, 8))
        assertArrayEquals(expected.copyOfRange(12, 22), packet.copyOfRange(12, 22))
        assertCrcValid(packet)
    }

    @Test
    fun `port info for an empty slot reports disconnected`() {
        val packet = encoder.portInfoResponse(slot = 2, player = null)

        assertEquals(32, packet.size)
        assertEquals(16, littleEndian(packet).getShort(6).toInt())
        assertEquals(0x100001, littleEndian(packet).getInt(16))
        assertEquals(2, packet[20].toInt())   // slot
        assertEquals(0, packet[21].toInt())   // not connected
        assertCrcValid(packet)
    }

    @Test
    fun `pad data is 100 bytes with payload length 84`() {
        val packet = encoder.padData(buffer(), pairedPlayer(), packetNumber = 1, motionTimestampMicros = 0)

        assertEquals(100, packet.size)
        assertEquals(84, littleEndian(packet).getShort(6).toInt())
        assertEquals(0x100002, littleEndian(packet).getInt(16))
        assertCrcValid(packet)
    }

    @Test
    fun `controller header carries slot, connected state, and the right joycon MAC`() {
        val packet = encoder.padData(buffer(), pairedPlayer(), 1, 0)

        assertEquals(0, packet[20].toInt())  // P1 → slot 0
        assertEquals(2, packet[21].toInt())  // connected
        assertEquals(2, packet[22].toInt())  // full gyro
        assertEquals(2, packet[23].toInt())  // bluetooth
        val expectedMac = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0xEE.toByte(), 0x02)
        assertArrayEquals(expectedMac, packet.copyOfRange(24, 30))
        assertEquals(0x05, packet[30].toInt()) // 3.6 V → 100% → full
        assertEquals(1, packet[31].toInt())    // is-connected flag
    }

    @Test
    fun `buttons map to the DS4 bitmasks and analog bytes`() {
        val packet = encoder.padData(buffer(), pairedPlayer(), 1, 0)

        // left joycon holds Up + Minus → 0x10 | 0x01; right holds A + ZR → 0x20 | 0x02
        assertEquals(0x11, packet[36].toInt())
        assertEquals(0x22, packet[37].toInt())
        assertEquals(0xFF, packet[47].toInt() and 0xFF) // analog D-Pad Up
        assertEquals(0xFF, packet[50].toInt() and 0xFF) // analog A
        assertEquals(0xFF, packet[54].toInt() and 0xFF) // analog R2
    }

    @Test
    fun `sticks scale 0-4095 to 0-255 without inverting Y`() {
        val packet = encoder.padData(buffer(), pairedPlayer(), 1, 0)

        assertEquals(255, packet[40].toInt() and 0xFF) // left X full right
        assertEquals(128, packet[41].toInt() and 0xFF) // left Y centered
        assertEquals(128, packet[42].toInt() and 0xFF)
        assertEquals(128, packet[43].toInt() and 0xFF)
    }

    @Test
    fun `motion block carries the timestamp and converted IMU values`() {
        val state = pairedPlayer()
        val packet = encoder.padData(buffer(), state, 1, motionTimestampMicros = 123_456_789L)

        val body = littleEndian(packet)
        assertEquals(123_456_789L, body.getLong(68))
        val motion = MotionConverter.convert(state.motionSource?.input)
        assertEquals(motion.accelX, body.getFloat(76), 0f)
        assertEquals(motion.accelY, body.getFloat(80), 0f)
        assertEquals(motion.accelZ, body.getFloat(84), 0f)
        assertEquals(motion.gyroPitch, body.getFloat(88), 0f)
        assertEquals(motion.gyroYaw, body.getFloat(92), 0f)
        assertEquals(motion.gyroRoll, body.getFloat(96), 0f)
    }

    @Test
    fun `packet number is written little-endian`() {
        val packet = encoder.padData(buffer(), pairedPlayer(), packetNumber = 0x01020304L, motionTimestampMicros = 0)

        assertEquals(0x01020304, littleEndian(packet).getInt(32))
    }

    private fun buffer() = ByteArray(DsuPacketEncoder.PAD_DATA_PACKET_SIZE)

    private fun littleEndian(packet: ByteArray): ByteBuffer =
        ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)

    private fun assertCrcValid(packet: ByteArray) {
        val stored = littleEndian(packet).getInt(8)
        val zeroed = packet.copyOf().also { ByteBuffer.wrap(it).order(ByteOrder.LITTLE_ENDIAN).putInt(8, 0) }
        val expected = CRC32().apply { update(zeroed) }.value.toInt()
        assertEquals(expected, stored)
    }

    private fun pairedPlayer() = PlayerState(
        player = PlayerNumber.P1,
        left = ConnectedJoycon(
            address = "AA:BB:CC:DD:EE:01",
            side = Side.LEFT,
            deviceName = "Joy-Con (L)",
            input = JoyconInput(
                pressed = setOf(JoyconButton.Up.id, JoyconButton.Minus.id),
                stickX = 4095,
                stickY = 2048,
            ),
        ),
        right = ConnectedJoycon(
            address = "AA:BB:CC:DD:EE:02",
            side = Side.RIGHT,
            deviceName = "Joy-Con (R)",
            input = JoyconInput(
                pressed = setOf(JoyconButton.A.id, JoyconButton.ZR.id),
                accelX = 4096,
                gyroZ = 1000,
                batteryVolts = 3.6f,
            ),
        ),
    )
}
