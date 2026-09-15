package com.joegec.joycon2android.connection.console

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/** Holds the link open after Android's pairing fails: docs/protocol.md#android-workarounds */
@SuppressLint("MissingPermission")
internal class LinkHolder(private val device: BluetoothDevice, private val label: String) {

    companion object {
        private const val TAG = "Joycon2"
        private const val PSM = 0x0080
        private const val QUICK_FAILURE_MS = 1_000L
        private const val RETRY_DELAY_MS = 200L
    }

    @Volatile private var running = false
    private val sockets = ConcurrentHashMap.newKeySet<BluetoothSocket>()

    fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || running) return
        running = true
        Thread({
            while (running) {
                val took = attempt()
                if (took < QUICK_FAILURE_MS) SystemClock.sleep(RETRY_DELAY_MS)
            }
        }, "link-holder-$label").start()
    }

    /** Adds an extra pending channel right away, for the moment Android's pairing gives up. */
    fun holdNow() {
        if (!running) return
        Thread({ attempt() }, "link-holder-now-$label").start()
    }

    fun stop() {
        running = false
        sockets.forEach { runCatching { it.close() } }
        sockets.clear()
    }

    private fun attempt(): Long {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return 0
        val started = SystemClock.elapsedRealtime()
        var socket: BluetoothSocket? = null
        try {
            socket = device.createInsecureL2capChannel(PSM)
            sockets.add(socket)
            if (running) socket.connect()
        } catch (e: Exception) {
            Log.v(TAG, "[$label] link hold attempt ended: ${e.message}")
        } finally {
            socket?.let {
                sockets.remove(it)
                runCatching { it.close() }
            }
        }
        return SystemClock.elapsedRealtime() - started
    }
}
