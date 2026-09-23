package com.joegec.joycon2android.ble

import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** The host address console-protocol pairing stores; apps are handed 02:00:00:00:00:00. Blocks. */
class HostBluetoothAddress(private val acquireShell: ((PrivilegedShell?) -> Unit) -> Unit) {

    private companion object {
        const val ACQUIRE_TIMEOUT_SECONDS = 30L
        val ADDRESS_PATTERN = Regex("^([0-9A-F]{2}:){5}[0-9A-F]{2}$")
    }

    @Volatile private var cached: String? = null

    fun read(): String? {
        cached?.let { return it }
        val shell = acquire() ?: return null
        val process = shell.shell("settings get secure bluetooth_address") ?: return null
        return try {
            process.outputStream.close()
            process.inputStream.bufferedReader().readText().trim().uppercase()
                .takeIf { ADDRESS_PATTERN.matches(it) }
                ?.also { cached = it }
        } catch (_: Exception) {
            null
        } finally {
            process.destroy()
        }
    }

    private fun acquire(): PrivilegedShell? {
        val latch = CountDownLatch(1)
        var shell: PrivilegedShell? = null
        acquireShell {
            shell = it
            latch.countDown()
        }
        return if (latch.await(ACQUIRE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) shell else null
    }
}
