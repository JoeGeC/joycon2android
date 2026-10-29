package com.joegec.joycon2android.connection.console

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.util.Log
import com.joegec.joycon2android.connection.ControllerCommands
import com.joegec.joycon2android.connection.ControllerProtocol
import com.joegec.joycon2android.connection.GattOpQueue
import com.joegec.joycon2android.connection.VibrationSample
import com.joegec.joycon2android.connection.SpiColorParser
import com.joegec.joycon2android.model.JoyconInput
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

/** Start-up sequence: docs/protocol.md#console-protocol-controllers */
@SuppressLint("MissingPermission")
internal class ConsoleSession(
    private val gatt: BluetoothGatt,
    private val channel: ConsoleChannel,
    private val ops: GattOpQueue,
    initialLedBitmask: Byte,
    private val onInput: (JoyconInput) -> Unit,
    private val onAccentColor: (Int) -> Unit,
    private val onReady: () -> Unit,
) : ControllerProtocol {
    companion object {
        private const val TAG = "Joycon2"
        private val SESSION_START_VALUE = byteArrayOf(0x01, 0x00)

        // Written to the input characteristic's 0x679d5510 descriptor before subscribing, as the console does.
        private val REPORT_RATE_VALUE = byteArrayOf(0x85.toByte(), 0x00)
    }

    private val label = channel.side.name
    private val worker = Executors.newSingleThreadExecutor { Thread(it, "console-$label") }
    private val transport = ConsoleTransport(gatt, channel, label, ops)
    private val linkHolder = LinkHolder(gatt.device, label)

    @Volatile private var stopped = false
    @Volatile private var ledBitmask = initialLedBitmask
    private var lastCounter = -1
    private var packetId = 0

    override fun start() {
        Log.i(TAG, "[$label] Using the console protocol")
        linkHolder.start()
        submit(::initialise)
    }

    override fun stop() {
        stopped = true
        linkHolder.stop()
        transport.close()
        ops.clear()
        worker.shutdownNow()
    }

    override fun onPairingFailed() {
        linkHolder.holdNow()
    }

    // 7.5 ms is worth far more here than on the common channel: docs/protocol.md#android-ble-gotchas
    override fun setHighPriority(enabled: Boolean) {
        if (enabled) {
            submit { ConnectionInterval.requestFastest(gatt) }
            return
        }
        val accepted = gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED)
        Log.i(TAG, "[$label] Connection priority high=false accepted=$accepted")
    }

    override fun setPlayerLed(bitmask: Byte) {
        ledBitmask = bitmask
        submit { transport.send(ControllerCommands.playerLed(bitmask)) }
    }

    override fun vibrate(sample: VibrationSample) {
        submit { transport.send(ControllerCommands.vibrationSample(sample)) }
    }

    override fun onCharacteristicChanged(uuid: UUID, value: ByteArray) {
        if (uuid == channel.input.uuid) onReport(value) else transport.onReply(value)
    }

    private fun onReport(report: ByteArray) {
        if (report.isEmpty()) return
        val counter = ConsolePacketParser.counter(report)
        packetId += if (lastCounter < 0) 1 else (counter - lastCounter + 256) % 256
        lastCounter = counter
        ConsolePacketParser.parse(report, channel.side, packetId)?.let(onInput)
    }

    private fun initialise() {
        openCommandChannel()
        identify()
        configure()
        if (!enableInput()) {
            Log.e(TAG, "[$label] Could not enable console input reports")
            return
        }
        Log.i(TAG, "[$label] Console init complete")
        if (!stopped) onReady()
    }

    private fun openCommandChannel() {
        ConsoleChannel.characteristic(gatt, ConsoleChannel.SESSION_START)?.let { transport.write(it, SESSION_START_VALUE) }
        ConsoleChannel.characteristic(gatt, ConsoleChannel.RESPONSE)?.let(transport::subscribe)
        channel.extendedResponse?.let(transport::subscribe)
    }

    private fun identify() {
        transport.send(ConsoleCommands.HELLO)
        transport.send(ControllerCommands.spiRead(0x40, ControllerCommands.DEVICE_INFO_ADDRESS))
            ?.let(SpiColorParser::parseAccentColor)
            ?.let(onAccentColor)
        transport.send(ConsoleCommands.FIRMWARE_INFO)
        transport.send(ConsoleCommands.UNKNOWN_16_01)
    }

    private fun configure() {
        transport.send(ConsoleCommands.VIBRATION_SAMPLE)
        transport.send(ControllerCommands.playerLed(ledBitmask))
        transport.send(ConsoleCommands.FEATURES_INIT)
        ConsoleCommands.SPI_READS_AFTER_FEATURES_INIT.forEach { (length, address) ->
            transport.send(ControllerCommands.spiRead(length, address))
        }
        transport.send(ConsoleCommands.UNKNOWN_11_03)
        transport.send(
            ControllerCommands.spiRead(
                ConsoleCommands.SPI_READ_BEFORE_VIBRATION_LENGTH,
                ConsoleCommands.SPI_READ_BEFORE_VIBRATION,
            ),
        )
        transport.send(ConsoleCommands.VIBRATION_DATA)
        transport.send(ConsoleCommands.UNKNOWN_11_01)
        transport.send(ConsoleCommands.FEATURES_ENABLE)
    }

    private fun enableInput(): Boolean {
        channel.input.getDescriptor(ConsoleChannel.REPORT_RATE)?.let { transport.writeDescriptor(it, REPORT_RATE_VALUE) }
        return transport.subscribe(channel.input) && !stopped
    }

    private fun submit(task: () -> Unit) {
        if (stopped) return
        try {
            worker.execute {
                try {
                    task()
                } catch (_: InterruptedException) {
                }
            }
        } catch (_: RejectedExecutionException) {
        }
    }
}
