package com.joegec.joycon2android.ble

import java.util.ArrayDeque

/**
 * Serializes GATT operations. Android's BluetoothGatt allows only one
 * outstanding write/descriptor-write at a time — issuing a second before
 * the callback fires silently drops it.
 */
class GattOpQueue {

    private val queue = ArrayDeque<() -> Unit>()
    private var inFlight = false

    fun enqueue(op: () -> Unit) {
        queue.add(op)
        runNext()
    }

    fun complete() {
        inFlight = false
        runNext()
    }

    fun clear() {
        queue.clear()
        inFlight = false
    }

    private fun runNext() {
        if (inFlight) return
        val op = queue.poll() ?: return
        inFlight = true
        op()
    }
}
