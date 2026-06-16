package com.joegec.joycon2android.dsu
import com.joegec.joycon2android.dsu.motion.GyroCalibrator
import com.joegec.joycon2android.dsu.motion.MotionConverter

import android.os.SystemClock
import android.util.Log
import com.joegec.joycon2android.model.PlayerState
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class DsuServer(
    private val scope: CoroutineScope,
    private val port: Int = DsuConfig.PORT,
    private val timestampMicros: () -> Long = { SystemClock.elapsedRealtimeNanos() / 1_000 },
) : DsuRepository {

    private val calibrator = GyroCalibrator()
    private val encoder = DsuPacketEncoder(serverId = Random.nextInt()) { state ->
        val source = state.motionSource
        MotionConverter.convert(source?.let { calibrator.calibrate(it.address, it.input) })
    }
    private val registry = DsuClientRegistry()
    private val sendBuffer = ByteArray(DsuPacketEncoder.PAD_DATA_PACKET_SIZE)
    private val packetCounters = LongArray(DsuPacketEncoder.SLOT_COUNT)

    // Pad batches ride a buffered channel instead of a StateFlow: conflation would drop
    // motion samples, and UDP sends can't run on the synchronous onState (main) thread
    private val batches = Channel<PadDataBatch>(BATCH_BUFFER, BufferOverflow.DROP_OLDEST)

    private var socket: DatagramSocket? = null
    private var serverJob: Job? = null

    @Volatile
    private var latestPlayers: List<PlayerState> = emptyList()

    private val _enabled = MutableStateFlow(false)
    override val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val _clientCount = MutableStateFlow(0)
    override val clientCount: StateFlow<Int> = _clientCount.asStateFlow()

    private val _address = MutableStateFlow<String?>(null)
    override val address: StateFlow<String?> = _address.asStateFlow()

    override fun enable() {
        if (_enabled.value) return
        val bound = try {
            DatagramSocket(InetSocketAddress(bindAddress(), port))
        } catch (e: Exception) {
            _error.value = "Could not open UDP port $port: ${e.message}"
            return
        }
        Log.i(TAG, "Listening on ${bound.localSocketAddress}")
        socket = bound
        drainStaleBatches()
        serverJob = scope.launch(Dispatchers.IO) {
            launch { receiveLoop(bound) }
            launch { sendLoop(bound) }
        }
        _enabled.value = true
        _error.value = null
        _address.value = currentAddress()
    }

    override fun disable() {
        _enabled.value = false
        socket?.close()
        socket = null
        serverJob?.cancel()
        serverJob = null
        registry.clear()
        _clientCount.value = 0
        _address.value = null
        packetCounters.fill(0)
    }

    override fun push(players: List<PlayerState>) {
        latestPlayers = players
        if (!_enabled.value) return
        batches.trySend(PadDataBatch(players, timestampMicros()))
    }

    // Emulators dial the IPv4 address we advertise; getLoopbackAddress() resolves to
    // IPv6 ::1 on Android, and a socket bound there never sees 127.0.0.1 datagrams
    private fun bindAddress(): InetAddress = InetAddress.getByAddress(byteArrayOf(127, 0, 0, 1))

    private fun currentAddress(): String = "127.0.0.1:$port"

    private fun drainStaleBatches() {
        while (batches.tryReceive().isSuccess) Unit
    }

    private fun receiveLoop(socket: DatagramSocket) {
        val datagram = DatagramPacket(ByteArray(RECEIVE_BUFFER_SIZE), RECEIVE_BUFFER_SIZE)
        while (!socket.isClosed) {
            try {
                // receive() shrinks the packet to the last datagram's size; without a reset
                // every following packet that is longer gets truncated and fails its CRC
                datagram.setLength(RECEIVE_BUFFER_SIZE)
                socket.receive(datagram)
                handleRequest(socket, datagram)
            } catch (e: IOException) {
                if (socket.isClosed) return
            }
        }
    }

    private fun handleRequest(socket: DatagramSocket, datagram: DatagramPacket) {
        when (val request = DsuRequestParser.parse(datagram.data, datagram.length)) {
            DsuRequest.Version -> reply(socket, encoder.versionResponse(), datagram)
            is DsuRequest.PortInfo -> request.slots
                .filter { it in packetCounters.indices }
                .forEach { slot -> reply(socket, encoder.portInfoResponse(slot, playerInSlot(slot)), datagram) }
            is DsuRequest.PadData -> {
                if (registry.register(datagram.socketAddress, request, timestampMicros() / 1_000)) {
                    Log.i(TAG, "Client subscribed: ${datagram.socketAddress}")
                }
                _clientCount.value = registry.size
            }
            null -> Log.w(TAG, "Dropped invalid ${datagram.length}-byte packet from ${datagram.socketAddress}")
        }
    }

    private fun reply(socket: DatagramSocket, packet: ByteArray, request: DatagramPacket) {
        socket.send(DatagramPacket(packet, packet.size, request.socketAddress))
    }

    private fun playerInSlot(slot: Int): PlayerState? =
        latestPlayers.firstOrNull { it.player.index - 1 == slot }

    private suspend fun sendLoop(socket: DatagramSocket) {
        var sent = 0L
        for (batch in batches) {
            val nowMillis = batch.timestampMicros / 1_000
            for (player in batch.players) {
                val slot = player.player.index - 1
                if (slot !in packetCounters.indices) continue // DSU has 4 slots; P5–P8 are not served
                val recipients = registry.recipientsFor(slot, nowMillis)
                if (recipients.isEmpty()) continue
                val packet = encoder.padData(sendBuffer, player, ++packetCounters[slot], batch.timestampMicros)
                for (client in recipients) {
                    try {
                        socket.send(DatagramPacket(packet, packet.size, client))
                        if (++sent % LOG_EVERY_PACKETS == 0L) {
                            Log.i(TAG, "Streaming: $sent pad packets sent, ${registry.size} client(s)")
                        }
                    } catch (e: IOException) {
                        if (socket.isClosed) return
                    }
                }
            }
            _clientCount.value = registry.size
        }
    }

    companion object {
        private const val TAG = "DsuServer"
        private const val RECEIVE_BUFFER_SIZE = 128
        private const val BATCH_BUFFER = 64
        private const val LOG_EVERY_PACKETS = 1024L
    }
}

private data class PadDataBatch(val players: List<PlayerState>, val timestampMicros: Long)
