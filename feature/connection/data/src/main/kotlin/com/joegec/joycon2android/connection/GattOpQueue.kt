package com.joegec.joycon2android.connection

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.ArrayDeque

/** Android silently drops a second outstanding GATT op. Advances after [TIMEOUT_MS] if a callback never comes. */
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
