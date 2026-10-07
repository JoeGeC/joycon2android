package com.joegec.joycon2android.connection.console

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothStatusCodes
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.joegec.joycon2android.connection.ControllerCommands
import com.joegec.joycon2android.connection.GattOpQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** Blocking request/reply on the console channel, so callers must stay off the main thread. */
@SuppressLint("MissingPermission")
internal class ConsoleTransport(
    private val gatt: BluetoothGatt,
    private val channel: ConsoleChannel,
    private val label: String,
    private val ops: GattOpQueue,
) {
    companion object {
        private const val TAG = "Joycon2"
        private const val REPLY_TIMEOUT_MS = 700L
        private const val OPERATION_TIMEOUT_MS = 3_000L
    }

    private val replies = LinkedBlockingQueue<ByteArray>()
    @Volatile private var closed = false

    fun close() {
        closed = true
    }

    fun onReply(value: ByteArray) {
        replies.offer(value)
    }

    /** Sends [command] and returns the reply from its header onwards, or null if none arrived. */
    fun send(command: ByteArray): ByteArray? {
        replies.clear()
        val frame = ByteArray(ConsoleChannel.COMMAND_PREFIX_LENGTH) + command
        if (!write(channel.command, frame)) return null
        val deadline = SystemClock.elapsedRealtime() + REPLY_TIMEOUT_MS
        while (!closed) {
            val remaining = deadline - SystemClock.elapsedRealtime()
            if (remaining <= 0) break
            val value = replies.poll(remaining, TimeUnit.MILLISECONDS) ?: break
            val start = replyStart(value, command)
            if (start >= 0) return value.copyOfRange(start, value.size)
        }
        Log.w(TAG, "[$label] No reply to ${hex(command.copyOf(ControllerCommands.HEADER_LENGTH))}")
        return null
    }

    fun subscribe(characteristic: BluetoothGattCharacteristic): Boolean {
        gatt.setCharacteristicNotification(characteristic, true)
        val cccd = characteristic.getDescriptor(ConsoleChannel.CCCD) ?: return false
        return writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
    }

    fun write(characteristic: BluetoothGattCharacteristic, value: ByteArray): Boolean {
        val type = if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        } else {
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }
        Log.d(TAG, "[$label] TX ${hex(value)}")
        return runOperation {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(characteristic, value, type) == BluetoothStatusCodes.SUCCESS
            } else {
                @Suppress("DEPRECATION")
                characteristic.writeType = type
                @Suppress("DEPRECATION")
                characteristic.value = value
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(characteristic)
            }
        }
    }

    fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray): Boolean = runOperation {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, value) == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = value
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
    }

    // A reply echoes the command id at [0] and sub-command at [3], with 0x01 at [1].
    private fun replyStart(value: ByteArray, command: ByteArray): Int {
        for (i in 0..value.size - ControllerCommands.HEADER_LENGTH) {
            if (value[i] == command[0] && value[i + 1] == ControllerCommands.REPLY_MARKER && value[i + 3] == command[3]) {
                return i
            }
        }
        return -1
    }

    private fun runOperation(start: () -> Boolean): Boolean {
        if (closed) return false
        return ops.awaitOp(OPERATION_TIMEOUT_MS, start) == BluetoothGatt.GATT_SUCCESS
    }

    private fun hex(bytes: ByteArray) = bytes.joinToString(" ") { "%02X".format(it) }
}
