package com.joegec.joycon2android.gamepad

import android.content.Context
import android.util.Log
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import com.joegec.joycon2android.gamepad.privileged.ShellProcess
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Drives `uinput_relay.c`: docs/virtual-gamepad.md#uinput-relay */
class UinputRelay(
    private val device: UinputDevice,
    private val onRumble: (strong: Int, weak: Int) -> Unit,
) {

    private var process: ShellProcess? = null
    private var output: OutputStream? = null

    // Reused at input rate.
    private val events = ByteBuffer.allocate(MAX_MESSAGE).order(ByteOrder.LITTLE_ENDIAN)

    fun create(context: Context, shell: PrivilegedShell): Boolean = try {
        val started = shell.newProcess(arrayOf(BINARY.deploy(context, shell)))
            ?: throw IOException("Privileged shell returned no process")
        process = started
        output = started.outputStream
        writeMessage(setupMessage())
        val replies = started.inputStream.bufferedReader()
        if (replies.readLine() != READY) throw IOException("Relay didn't create ${device.name}")
        Thread({ replies.forEachLine(::onReply) }, "uinput-${device.name}").apply { isDaemon = true }.start()
        Log.i(TAG, "uinput relay started for ${device.name}")
        true
    } catch (e: IOException) {
        Log.e(TAG, "Failed to start uinput relay", e)
        destroy()
        false
    }

    fun send(frame: List<InputEvent>): Boolean = try {
        events.clear()
        events.put(EVENTS).putShort(frame.size.toShort())
        frame.forEach { events.putShort(it.type.toShort()).putShort(it.code.toShort()).putInt(it.value) }
        writeMessage(events)
        true
    } catch (e: IOException) {
        Log.e(TAG, "Failed to send events", e)
        false
    }

    fun destroy() {
        try {
            output?.let { stream ->
                stream.write(ByteArray(4))
                stream.flush()
                stream.close()
            }
        } catch (_: IOException) {
        }
        output = null
        process?.destroy()
        process = null
        Log.i(TAG, "uinput relay stopped for ${device.name}")
    }

    private fun onReply(line: String) {
        val parts = line.split(' ')
        if (parts.size != 3 || parts[0] != RUMBLE) return
        val strong = parts[1].toIntOrNull() ?: return
        val weak = parts[2].toIntOrNull() ?: return
        onRumble(strong, weak)
    }

    private fun writeMessage(message: ByteBuffer) {
        val stream = output ?: throw IOException("Relay not running")
        val length = message.position()
        val header = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(length)
        stream.write(header.array())
        stream.write(message.array(), 0, length)
        stream.flush()
    }

    private fun setupMessage(): ByteBuffer = ByteBuffer.allocate(MAX_MESSAGE).order(ByteOrder.LITTLE_ENDIAN).apply {
        put(SETUP)
        putShort(Evdev.BUS_USB.toShort())
        putShort(device.vendorId.toShort())
        putShort(device.productId.toShort())
        putShort(device.version.toShort())
        putString(device.name)
        putString(device.phys)
        putShort(device.keys.size.toShort())
        device.keys.forEach { putShort(it.toShort()) }
        putShort(device.axes.size.toShort())
        device.axes.forEach {
            putShort(it.code.toShort())
            putInt(it.min).putInt(it.max).putInt(it.fuzz).putInt(it.flat).putInt(it.resolution)
        }
        put(device.rumbleEffects.toByte())
    }

    private fun ByteBuffer.putString(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        put(bytes.size.toByte())
        put(bytes)
    }

    private companion object {
        const val TAG = "UinputRelay"
        val BINARY = RelayBinary("uinput_relay", "/data/local/tmp/.uinput_relay")

        const val MAX_MESSAGE = 4096
        const val SETUP: Byte = 1
        const val EVENTS: Byte = 2
        const val READY = "OK"
        const val RUMBLE = "R"
    }
}
