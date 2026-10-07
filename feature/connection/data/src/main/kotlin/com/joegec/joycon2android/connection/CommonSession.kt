package com.joegec.joycon2android.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothStatusCodes
import android.os.Build
import android.util.Log
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side
import java.util.UUID

/** Start-up sequence: docs/protocol.md#input-report */
@SuppressLint("MissingPermission")
internal class CommonSession(
    private val gatt: BluetoothGatt,
    private val channel: CommonChannel,
    private val side: Side,
    private val ops: GattOpQueue,
    initialLedBitmask: Byte,
    private val onInput: (JoyconInput) -> Unit,
    private val onAccentColor: (Int) -> Unit,
    private val onReady: () -> Unit,
) : ControllerProtocol {
    companion object {
        private const val TAG = "Joycon2"

        // A controller misses an init command sent too soon after the one before.
        private const val INIT_GAP_MS = 500L

        private val ALL_FEATURES: Byte = 0xFF.toByte()
        private val FEATURES_INIT = ControllerCommands.selectFeatures(ALL_FEATURES)
        private val FEATURES_ENABLE = ControllerCommands.enableFeatures(ALL_FEATURES)

        // 0x40 bytes of the DeviceInfo block: docs/protocol.md#spi-reads. This path has always
        // sent transport 0x00 (USB) here, and a Joy-Con 2 answers it.
        private val SPI_READ_COLOR = ControllerCommands.spiRead(
            0x40, ControllerCommands.DEVICE_INFO_ADDRESS, ControllerCommands.USB,
        )
    }

    @Volatile private var ready = false
    @Volatile private var ledBitmask = initialLedBitmask
    private var ledSentAfterFirstPacket = false

    override fun start() {
        channel.write.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        subscribeCommandResponse()
        subscribeInput()
        ops.enqueue(INIT_GAP_MS) { write(FEATURES_INIT) }
        ops.enqueue(INIT_GAP_MS) { write(FEATURES_ENABLE) }
        ops.enqueue(INIT_GAP_MS) { write(SPI_READ_COLOR) }
        ops.enqueue {
            ready = true
            Log.i(TAG, "[$side] Init sequence complete")
            onReady()
            false // no GATT op — advance immediately
        }
    }

    override fun stop() {
        ready = false
    }

    /** The console detour ended, so this channel is driving the controller again. */
    fun resume() {
        ready = true
    }

    override fun onCharacteristicChanged(uuid: UUID, value: ByteArray) {
        when (uuid) {
            CommonChannel.INPUT -> onReport(value)
            CommonChannel.COMMAND_RESPONSE -> onCommandResponse(value)
        }
    }

    override fun setPlayerLed(bitmask: Byte) {
        ledBitmask = bitmask
        if (ready) sendPlayerLed()
    }

    override fun setHighPriority(enabled: Boolean) {
        val priority = if (enabled) {
            BluetoothGatt.CONNECTION_PRIORITY_HIGH
        } else {
            BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        }
        Log.i(TAG, "[$side] Connection priority high=$enabled accepted=${gatt.requestConnectionPriority(priority)}")
    }

    private fun onReport(report: ByteArray) {
        PacketParser.parse(report, side)?.let(onInput)
        if (!ledSentAfterFirstPacket && ready) {
            ledSentAfterFirstPacket = true
            sendPlayerLed()
        }
    }

    private fun onCommandResponse(value: ByteArray) {
        Log.d(TAG, "[$side] Cmd response: ${value.joinToString(" ") { "%02X".format(it) }}")
        SpiColorParser.parseAccentColor(value)?.let { color ->
            Log.i(TAG, "[$side] Accent color: #${"%06X".format(color)}")
            onAccentColor(color)
        }
    }

    private fun sendPlayerLed() {
        ops.enqueue {
            val cmd = ControllerCommands.playerLed(ledBitmask)
            Log.i(TAG, "[$side] Sending LED cmd: ${cmd.joinToString(" ") { "%02X".format(it) }}")
            write(cmd)
        }
    }

    private fun subscribeCommandResponse() {
        val characteristic = channel.commandResponse ?: return
        gatt.setCharacteristicNotification(characteristic, true)
        val cccd = characteristic.getDescriptor(CommonChannel.CCCD)
        if (cccd == null) {
            Log.w(TAG, "[$side] CMD_RESPONSE char has no CCCD descriptor")
            return
        }
        ops.enqueue {
            Log.d(TAG, "[$side] Writing CMD_RESPONSE CCCD")
            writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        }
    }

    private fun subscribeInput() {
        gatt.setCharacteristicNotification(channel.input, true)
        val cccd = channel.input.getDescriptor(CommonChannel.CCCD)
        if (cccd == null) {
            Log.e(TAG, "[$side] NOTIFY char has no CCCD descriptor — notifications won't work!")
            return
        }
        ops.enqueue {
            Log.d(TAG, "[$side] Writing NOTIFY CCCD")
            writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        }
    }

    private fun write(value: ByteArray): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                channel.write, value, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            ) == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            channel.write.value = value
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(channel.write)
        }

    private fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, value) == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = value
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
}
