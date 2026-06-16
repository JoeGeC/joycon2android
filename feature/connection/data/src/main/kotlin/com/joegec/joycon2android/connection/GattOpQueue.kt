package com.joegec.joycon2android.connection

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.ArrayDeque

/**
 * Serializes GATT operations. Android's BluetoothGatt allows only one
 * outstanding write/descriptor-write at a time — issuing a second before
 * the callback fires silently drops it.
 *
 * Includes a safety timeout: if a callback never arrives (e.g. writeCharacteristic
 * returned false), the queue advances after [TIMEOUT_MS] to avoid permanent stalls.
 */
class GattOpQueue {

    companion object {
        private const val TAG = "GattOpQueue"
        private const val TIMEOUT_MS = 2000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val queue = ArrayDeque<() -> Boolean>()
    private var inFlight = false
    private val timeoutRunnable = Runnable {
        Log.w(TAG, "Op timed out — advancing queue")
        inFlight = false
        runNext()
    }

    fun enqueue(op: () -> Boolean) {
        queue.add(op)
        runNext()
    }

    fun complete() {
        handler.removeCallbacks(timeoutRunnable)
        inFlight = false
        runNext()
    }

    fun clear() {
        handler.removeCallbacks(timeoutRunnable)
        queue.clear()
        inFlight = false
    }

    private fun runNext() {
        if (inFlight) return
        val op = queue.poll() ?: return
        inFlight = true
        val success = op()
        if (!success) {
            Log.w(TAG, "Op returned false — advancing queue immediately")
            inFlight = false
            runNext()
        } else {
            handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
        }
    }
}
