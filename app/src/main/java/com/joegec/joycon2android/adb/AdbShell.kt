package com.joegec.joycon2android.adb

import android.content.Context
import com.joegec.joycon2android.uhid.PrivilegedShell
import com.joegec.joycon2android.uhid.ShellProcess
import io.github.muntashirakon.adb.AdbStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Runs shell-uid processes through an in-app ADB connection to the device's own
 * wireless-debugging daemon — the Shizuku-free path to `/dev/uhid`. Uses the raw
 * `exec:` service (not `shell:`, whose PTY would mangle the binary UHID stream).
 */
class AdbShell(context: Context) : PrivilegedShell {

    private val manager = AdbConnectionManager.getInstance(context)

    override val isReady: Boolean
        get() = try {
            manager.isConnected
        } catch (_: Exception) {
            false
        }

    /** Registers our key with the daemon. Blocking + throwing — call off the main thread. */
    fun pair(host: String, port: Int, code: String): Boolean = manager.pair(host, port, code)

    /** Connects with our trusted key. Throws AdbPairingRequiredException if not yet paired. */
    fun connect(host: String, port: Int): Boolean = manager.connect(host, port)

    fun disconnect() {
        try {
            manager.disconnect()
        } catch (_: Exception) {
        }
    }

    override fun newProcess(argv: Array<String>): ShellProcess? {
        val stream = manager.openStream("exec:" + argv.joinToString(" "))
        return AdbProcess(stream)
    }

    private class AdbProcess(private val stream: AdbStream) : ShellProcess {
        override val outputStream: OutputStream = stream.openOutputStream()
        override val inputStream: InputStream = stream.openInputStream()

        override fun waitFor() {
            val buf = ByteArray(256)
            try {
                while (inputStream.read(buf) >= 0) Unit
            } catch (_: Exception) {
            }
        }

        override fun destroy() {
            try {
                stream.close()
            } catch (_: Exception) {
            }
        }
    }
}
