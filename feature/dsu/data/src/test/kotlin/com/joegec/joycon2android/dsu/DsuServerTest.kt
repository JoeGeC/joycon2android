package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Drives the real server over loopback UDP, as a DSU client like Dolphin would. */
class DsuServerTest {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val port = freeUdpPort()
    private val server = DsuServer(scope, port) { 1_000_000L }
    private lateinit var client: DatagramSocket

    // IPv4 throughout: the server must answer at the 127.0.0.1 address emulators dial,
    // not just whatever getLoopbackAddress() resolves to (::1 on Android)
    private val loopback: InetAddress = InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1))

    @Before
    fun start() {
        server.enable()
        assertTrue(server.enabled.value)
        client = DatagramSocket(0, loopback).apply { soTimeout = 2_000 }
    }

    @After
    fun stop() {
        client.close()
        server.disable()
        scope.cancel()
    }

    @Test
    fun `answers a version request`() {
        send(clientPacket(0x100000))

        val response = receive()

        assertEquals(0x100000, typeOf(response))
        assertEquals(1001, littleEndian(response).getShort(20).toInt())
    }

    @Test
    fun `reports a pushed player as connected in port info`() {
        server.push(listOf(player()))
        send(clientPacket(0x100001, portInfoPayload(slots = listOf(0))))

        val response = receive()

        assertEquals(0x100001, typeOf(response))
        assertEquals(0, response[20].toInt()) // slot
        assertEquals(2, response[21].toInt()) // connected
    }

    @Test
    fun `streams pad data to a subscribed client`() {
        send(clientPacket(0x100002, ByteArray(8)))
        awaitSubscription()

        server.push(listOf(player()))
        val response = receive()

        assertEquals(0x100002, typeOf(response))
        assertEquals(100, response.size)
        assertEquals(1, response[31].toInt()) // is-connected flag
    }

    @Test
    fun `slot-based subscribers only receive their slot`() {
        // Subscribe to slot 1 only (flags=1, slot=1); both players get pushed
        send(clientPacket(0x100002, ByteArray(8).also { it[0] = 1; it[1] = 1 }))
        awaitSubscription()

        repeat(4) { server.push(listOf(player(), playerTwo())) }

        repeat(4) {
            val response = receive()
            assertEquals(1, response[20].toInt()) // slot byte — never slot 0
        }
    }

    @Test
    fun `handles packets of different sizes back to back`() {
        // A 20-byte version request must not truncate the 28-byte requests after it
        send(clientPacket(0x100000))
        receive()

        send(clientPacket(0x100001, portInfoPayload(slots = listOf(0))))
        assertEquals(0x100001, typeOf(receive()))

        send(clientPacket(0x100002, ByteArray(8)))
        awaitSubscription()
    }

    private fun awaitSubscription() {
        val deadline = System.currentTimeMillis() + 2_000
        while (server.clientCount.value == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
        assertEquals(1, server.clientCount.value)
    }

    private fun send(packet: ByteArray) {
        val serverAddress = InetSocketAddress(loopback, port)
        client.send(DatagramPacket(packet, packet.size, serverAddress))
    }

    private fun receive(): ByteArray {
        val datagram = DatagramPacket(ByteArray(256), 256)
        client.receive(datagram)
        return datagram.data.copyOf(datagram.length)
    }

    private fun typeOf(packet: ByteArray): Int = littleEndian(packet).getInt(16)

    private fun littleEndian(packet: ByteArray): ByteBuffer =
        ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)

    private fun portInfoPayload(slots: List<Int>): ByteArray =
        ByteBuffer.allocate(4 + slots.size).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(slots.size)
            .apply { slots.forEach { put(it.toByte()) } }
            .array()

    private fun clientPacket(type: Int, payload: ByteArray = ByteArray(0)): ByteArray {
        val packet = ByteBuffer.allocate(20 + payload.size).order(ByteOrder.LITTLE_ENDIAN)
            .put("DSUC".toByteArray(Charsets.US_ASCII))
            .putShort(1001)
            .putShort((4 + payload.size).toShort())
            .putInt(0)
            .putInt(0x0BADCAFE.toInt())
            .putInt(type)
            .put(payload)
            .array()
        val crc = CRC32().apply { update(packet) }.value.toInt()
        ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN).putInt(8, crc)
        return packet
    }

    private fun player() = PlayerState(
        player = PlayerNumber.P1,
        right = ConnectedJoycon(
            address = "AA:BB:CC:DD:EE:02",
            side = Side.RIGHT,
            deviceName = "Joy-Con (R)",
            input = JoyconInput(accelX = 4096, batteryVolts = 3.6f),
        ),
    )

    private fun playerTwo() = PlayerState(
        player = PlayerNumber.P2,
        left = ConnectedJoycon(
            address = "AA:BB:CC:DD:EE:01",
            side = Side.LEFT,
            deviceName = "Joy-Con (L)",
            input = JoyconInput(accelZ = 4096, batteryVolts = 3.5f),
        ),
    )

    private fun freeUdpPort(): Int {
        val probe = DatagramSocket(0)
        val port = probe.localPort
        probe.close()
        return port
    }
}
