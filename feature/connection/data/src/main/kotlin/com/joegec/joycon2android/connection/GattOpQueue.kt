package com.joegec.joycon2android.connection

import android.bluetooth.BluetoothGatt
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Android silently drops a second outstanding GATT op, so every op on a connection goes through
 * here. A callback releases the op that is in flight, never whatever else is waiting.
 */
class GattOpQueue {

    companion object {
        private const val TAG = "GattOpQueue"
        private const val TIMEOUT_MS = 2000L
    }

    private class Op(val run: () -> Boolean, val gapMs: Long, val finished: CountDownLatch?) {
        @Volatile var status = BluetoothGatt.GATT_FAILURE
    }

    private val handler = Handler(Looper.getMainLooper())
    private val queue = ArrayDeque<Op>()
    private var inFlight: Op? = null
    private val timeoutRunnable = Runnable {
        Log.w(TAG, "Op timed out — advancing queue")
        complete(BluetoothGatt.GATT_FAILURE)
    }

    /** Runs [op] when the queue reaches it, then waits [gapMs] before the next one. */
    fun enqueue(gapMs: Long = 0L, op: () -> Boolean) = add(Op(op, gapMs, null))

    /** Runs [op] and returns its status. Blocks, so callers must stay off the main thread. */
    fun awaitOp(timeoutMs: Long, op: () -> Boolean): Int {
        val entry = Op(op, 0L, CountDownLatch(1))
        add(entry)
        return if (entry.finished!!.await(timeoutMs, TimeUnit.MILLISECONDS)) {
            entry.status
        } else {
            BluetoothGatt.GATT_FAILURE
        }
    }

    @Synchronized
    fun complete(status: Int) {
        handler.removeCallbacks(timeoutRunnable)
        val op = inFlight ?: return
        inFlight = null
        op.status = status
        op.finished?.countDown()
        if (op.gapMs > 0L) handler.postDelayed({ runNext() }, op.gapMs) else runNext()
    }

    @Synchronized
    fun clear() {
        handler.removeCallbacks(timeoutRunnable)
        val abandoned = queue.toList() + listOfNotNull(inFlight)
        queue.clear()
        inFlight = null
        abandoned.forEach { it.finished?.countDown() }
    }

    @Synchronized
    private fun add(op: Op) {
        queue.add(op)
        runNext()
    }

    @Synchronized
    private fun runNext() {
        if (inFlight != null) return
        val op = queue.poll() ?: return
        inFlight = op
        handler.post { start(op) }
    }

    // Ops run on the main thread, and outside the lock: one of them reports the controller ready.
    private fun start(op: Op) {
        if (!isInFlight(op)) return
        if (op.run()) {
            handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
            return
        }
        Log.w(TAG, "Op returned false — advancing queue immediately")
        abandon(op)
    }

    @Synchronized
    private fun isInFlight(op: Op) = inFlight === op

    @Synchronized
    private fun abandon(op: Op) {
        if (inFlight !== op) return
        inFlight = null
        op.finished?.countDown()
        runNext()
    }
}
