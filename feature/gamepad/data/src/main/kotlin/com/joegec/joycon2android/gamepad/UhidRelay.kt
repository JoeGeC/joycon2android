package com.joegec.joycon2android.gamepad
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import com.joegec.joycon2android.gamepad.privileged.ShellProcess

import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UhidRelay(private val device: UhidDevice) {

    private var process: ShellProcess? = null
    private var outputStream: OutputStream? = null

    // Pre-allocated buffers for sendReport (called at ~60Hz)
    private val inputHeader = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    private val inputEventBuf = ByteBuffer.allocate(4 + 2 + MAX_REPORT_SIZE).order(ByteOrder.LITTLE_ENDIAN)

    fun create(context: Context, shell: PrivilegedShell): Boolean {
        return try {
            val relayPath = deployRelay(context, shell)
            val remoteProcess = shell.newProcess(arrayOf(relayPath))
                ?: run {
                    Log.e(TAG, "Privileged shell returned no process")
                    return false
                }
            process = remoteProcess
            val os = remoteProcess.outputStream

            // Wait for "OK\n" readiness signal from relay
            val inputStream = remoteProcess.inputStream
            val buf = ByteArray(3)
            var read = 0
            while (read < 3) {
                val n = inputStream.read(buf, read, 3 - read)
                if (n <= 0) {
                    Log.e(TAG, "Relay process closed before signalling readiness")
                    return false
                }
                read += n
            }

            val createEvent = buildCreateEvent()
            val header = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
            header.putInt(createEvent.size)
            os.write(header.array())
            os.write(createEvent)
            os.flush()

            outputStream = os
            Log.i(TAG, "UHID relay started for ${device.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start UHID relay", e)
            destroy()
            false
        }
    }

    fun sendReport(report: ByteArray): Boolean {
        val os = outputStream ?: return false
        return try {
            inputHeader.clear()
            inputHeader.putInt(4 + 2 + report.size)

            inputEventBuf.clear()
            inputEventBuf.putInt(UHID_INPUT2)
            inputEventBuf.putShort(report.size.toShort())
            inputEventBuf.put(report)

            os.write(inputHeader.array(), 0, 4)
            os.write(inputEventBuf.array(), 0, 4 + 2 + report.size)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to send report", e)
            false
        }
    }

    fun destroy() {
        try {
            outputStream?.let { os ->
                try {
                    // Send shutdown signal (length = 0)
                    val shutdown = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    shutdown.putInt(0)
                    os.write(shutdown.array())
                    os.flush()
                } catch (_: IOException) {}
                os.close()
            }
        } catch (_: IOException) {}
        outputStream = null
        process?.destroy()
        process = null
        Log.i(TAG, "UHID relay stopped for ${device.name}")
    }

    private fun buildCreateEvent(): ByteArray {
        val buf = ByteBuffer.allocate(UHID_EVENT_SIZE).order(ByteOrder.LITTLE_ENDIAN)

        buf.putInt(UHID_CREATE2)

        buf.putString(device.name, offset = 4, capacity = 128)
        buf.putString(device.phys, offset = 4 + 128, capacity = 64)
        buf.putString(device.uniq, offset = 4 + 128 + 64, capacity = 64)
        buf.position(4 + 128 + 64 + 64)

        buf.putShort(device.descriptor.size.toShort())
        buf.putShort(BUS_USB.toShort())
        buf.putInt(VENDOR_VIRTUAL)
        buf.putInt(device.productId)
        buf.putInt(1)       // version
        buf.putInt(0)       // country

        buf.put(device.descriptor)

        return buf.array()
    }

    // NUL-terminated within a fixed-size field.
    private fun ByteBuffer.putString(value: String, offset: Int, capacity: Int) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        position(offset)
        put(bytes, 0, minOf(bytes.size, capacity - 1))
    }

    companion object {
        private const val TAG = "UhidRelay"
        private const val RELAY_REMOTE_PATH = "/data/local/tmp/.uhid_relay"

        @Volatile
        private var relayDeployed = false

        private fun deployRelay(context: Context, shell: PrivilegedShell): String {
            if (relayDeployed) return RELAY_REMOTE_PATH

            val localPath = context.applicationInfo.nativeLibraryDir + "/libuhid_relay.so"

            // Copy from app's native lib dir to /data/local/tmp/ (shell-accessible)
            shell.newProcess(
                arrayOf("sh", "-c", "cp $localPath $RELAY_REMOTE_PATH && chmod 755 $RELAY_REMOTE_PATH"),
            )?.waitFor()
            relayDeployed = true
            return RELAY_REMOTE_PATH
        }

        private const val UHID_CREATE2 = 11
        private const val UHID_INPUT2 = 12
        private const val BUS_USB = 3
        private const val UHID_EVENT_SIZE = 4380
        private const val MAX_REPORT_SIZE = 64

        // Not Nintendo's: docs/virtual-gamepad.md#uhid
        private const val VENDOR_VIRTUAL = 0x1234
    }
}
