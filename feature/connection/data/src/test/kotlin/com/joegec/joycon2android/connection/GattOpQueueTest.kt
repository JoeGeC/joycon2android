package com.joegec.joycon2android.connection

import android.bluetooth.BluetoothGatt
import org.junit.Assert.assertEquals
import org.junit.Test

class GattOpQueueTest {

    private val scheduler = ManualScheduler()
    private val queue = GattOpQueue(scheduler)
    private val sent = mutableListOf<String>()

    private fun send(name: String): () -> Boolean = {
        sent += name
        true
    }

    @Test
    fun `sends the next op only after the callback for the one in flight`() {
        queue.enqueue(op = send("first"))
        queue.enqueue(op = send("second"))
        scheduler.runDue()
        assertEquals(listOf("first"), sent)

        queue.complete(BluetoothGatt.GATT_SUCCESS)
        scheduler.runDue()
        assertEquals(listOf("first", "second"), sent)
    }

    @Test
    fun `a cleared op still holds the queue until its own callback arrives`() {
        queue.enqueue(op = send("stale"))
        scheduler.runDue()
        queue.clear()
        queue.enqueue(op = send("fresh"))
        scheduler.runDue()
        assertEquals(listOf("stale"), sent)

        queue.complete(BluetoothGatt.GATT_SUCCESS) // the stale op's callback
        scheduler.runDue()
        assertEquals(listOf("stale", "fresh"), sent)
    }

    @Test
    fun `the stale callback does not answer the op sent after a clear`() {
        queue.enqueue(op = send("stale"))
        scheduler.runDue()
        queue.clear()
        queue.enqueue(op = send("fresh"))
        queue.enqueue(op = send("third"))
        queue.complete(BluetoothGatt.GATT_SUCCESS) // the stale op's callback
        scheduler.runDue()

        assertEquals(listOf("stale", "fresh"), sent)
    }

    @Test
    fun `a cleared op that was never sent is skipped`() {
        queue.enqueue(op = send("unsent"))
        queue.clear()
        queue.enqueue(op = send("fresh"))
        scheduler.runDue()

        assertEquals(listOf("fresh"), sent)
    }

    @Test
    fun `an op that times out releases the queue`() {
        queue.enqueue(op = send("silent"))
        queue.enqueue(op = send("next"))
        scheduler.runDue()
        scheduler.advanceBy(2_000)

        assertEquals(listOf("silent", "next"), sent)
    }

    @Test
    fun `an answered op's timeout never fires on a later op`() {
        queue.enqueue(op = send("quick"))
        scheduler.runDue()
        queue.complete(BluetoothGatt.GATT_SUCCESS)
        scheduler.advanceBy(1_500)
        queue.enqueue(op = send("slow"))
        queue.enqueue(op = send("after"))
        scheduler.runDue()
        scheduler.advanceBy(600) // past the quick op's 2 s

        assertEquals(listOf("quick", "slow"), sent)
    }

    @Test
    fun `waits the op's gap before the next one, even when another is added`() {
        queue.enqueue(gapMs = 500, op = send("paced"))
        scheduler.runDue()
        queue.complete(BluetoothGatt.GATT_SUCCESS)
        queue.enqueue(op = send("next"))
        scheduler.advanceBy(499)
        assertEquals(listOf("paced"), sent)

        scheduler.advanceBy(1)
        assertEquals(listOf("paced", "next"), sent)
    }

    @Test
    fun `an op that fails to send advances at once`() {
        queue.enqueue { sent += "refused"; false }
        queue.enqueue(op = send("next"))
        scheduler.runDue()

        assertEquals(listOf("refused", "next"), sent)
    }

    @Test
    fun `reset drops the op in flight, for a closed connection`() {
        queue.enqueue(op = send("orphan"))
        scheduler.runDue()
        queue.reset()
        queue.enqueue(op = send("fresh"))
        scheduler.runDue()

        assertEquals(listOf("orphan", "fresh"), sent)
    }
}
