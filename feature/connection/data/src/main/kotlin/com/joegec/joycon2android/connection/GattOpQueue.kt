package com.joegec.joycon2android.connection

import android.bluetooth.BluetoothGatt
import android.util.Log
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Android silently drops a second outstanding GATT op, so every op on a connection goes through
 * here. A callback can't say which op it answers, so a sent op holds the queue until its own
 * callback or timeout, even after [clear].
 */
internal class GattOpQueue(private val scheduler: OpScheduler = MainThreadScheduler()) {

    companion object {
        private const val TAG = "GattOpQueue"
        private const val TIMEOUT_MS = 2000L
    }

    private inner class Op(val run: () -> Boolean, val gapMs: Long) {
        val finished = CountDownLatch(1)
        @Volatile var status = BluetoothGatt.GATT_FAILURE
        var abandoned = false
        var answered = false
        val timeout = Runnable {
            Log.w(TAG, "Op timed out — advancing queue")
            answer(this, BluetoothGatt.GATT_FAILURE)
        }
    }

    private val queue = ArrayDeque<Op>()
    private var inFlight: Op? = null

    /** Runs [op] when the queue reaches it, then waits [gapMs] before the next one. */
    fun enqueue(gapMs: Long = 0L, op: () -> Boolean) = add(Op(op, gapMs))

    /** Runs [op] and returns its status. Blocks, so callers must stay off the main thread. */
    fun awaitOp(timeoutMs: Long, op: () -> Boolean): Int {
        val entry = Op(op, 0L)
        add(entry)
        return if (entry.finished.await(timeoutMs, TimeUnit.MILLISECONDS)) {
            entry.status
        } else {
            BluetoothGatt.GATT_FAILURE
        }
    }

    /** The GATT callback for the op in flight. */
    @Synchronized
    fun complete(status: Int) {
        inFlight?.let { answer(it, status) }
    }

    /** Drops every op not yet sent; one already sent still holds the queue until it is answered. */
    @Synchronized
    fun clear() {
        queue.forEach { it.finished.countDown() }
        queue.clear()
        inFlight?.let {
            it.abandoned = true
            it.finished.countDown()
        }
    }

    /** For a closed [BluetoothGatt], whose callbacks never come. */
    @Synchronized
    fun reset() {
        clear()
        inFlight?.let { scheduler.cancel(it.timeout) }
        inFlight = null
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
        scheduler.post { start(op) }
    }

    // Ops run outside the lock: one of them reports the controller ready.
    private fun start(op: Op) {
        if (!claimForSending(op)) return
        if (op.run()) return
        Log.w(TAG, "Op returned false — advancing queue immediately")
        skip(op)
    }

    // Arms the timeout before the op runs, so a callback that beats it can't leave it armed.
    @Synchronized
    private fun claimForSending(op: Op): Boolean {
        if (inFlight !== op) return false
        if (op.abandoned) {
            skip(op)
            return false
        }
        scheduler.postDelayed(op.timeout, TIMEOUT_MS)
        return true
    }

    @Synchronized
    private fun answer(op: Op, status: Int) {
        if (inFlight !== op || op.answered) return
        op.answered = true
        scheduler.cancel(op.timeout)
        op.status = status
        op.finished.countDown()
        if (op.gapMs > 0L) scheduler.postDelayed({ release(op) }, op.gapMs) else release(op)
    }

    @Synchronized
    private fun skip(op: Op) {
        if (inFlight !== op || op.answered) return
        op.answered = true
        scheduler.cancel(op.timeout)
        op.finished.countDown()
        release(op)
    }

    @Synchronized
    private fun release(op: Op) {
        if (inFlight !== op) return
        inFlight = null
        runNext()
    }
}
