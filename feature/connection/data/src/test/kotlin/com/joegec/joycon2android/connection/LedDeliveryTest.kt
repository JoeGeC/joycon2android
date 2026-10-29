package com.joegec.joycon2android.connection

import org.junit.Assert.assertEquals
import org.junit.Test

class LedDeliveryTest {

    private val scheduler = ManualScheduler()
    private var sends = 0
    private val delivery = LedDelivery(scheduler) { sends++ }

    // The controller's reply to 09/07, and to the vibration sample 0A/02.
    private val ledEcho = bytes(0x09, 0x01, 0x01, 0x07, 0x10, 0x78, 0x00, 0x00)
    private val clickEcho = bytes(0x0A, 0x01, 0x01, 0x02, 0x10, 0x78, 0x00, 0x00)

    @Test
    fun `an echoed LED is sent once`() {
        delivery.deliver()
        delivery.onReply(ledEcho)
        scheduler.advanceBy(5_000)

        assertEquals(1, sends)
    }

    @Test
    fun `an unanswered LED is sent again until echoed`() {
        delivery.deliver()
        scheduler.advanceBy(300)
        scheduler.advanceBy(300)
        delivery.onReply(ledEcho)
        scheduler.advanceBy(5_000)

        assertEquals(3, sends)
    }

    @Test
    fun `another command's reply is not the LED's echo`() {
        delivery.deliver()
        delivery.onReply(clickEcho)
        scheduler.advanceBy(300)

        assertEquals(2, sends)
    }

    @Test
    fun `it gives up after five attempts`() {
        delivery.deliver()
        repeat(10) { scheduler.advanceBy(300) }

        assertEquals(5, sends)
    }

    @Test
    fun `a stopped session sends nothing more`() {
        delivery.deliver()
        delivery.stop()
        scheduler.advanceBy(5_000)

        assertEquals(1, sends)
    }

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }
}
