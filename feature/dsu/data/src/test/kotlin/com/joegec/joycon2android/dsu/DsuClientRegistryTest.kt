package com.joegec.joycon2android.dsu

import java.net.InetSocketAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DsuClientRegistryTest {

    private val registry = DsuClientRegistry(timeoutMillis = 5_000)
    private val client = InetSocketAddress("127.0.0.1", 50000)
    private val other = InetSocketAddress("127.0.0.1", 50001)

    private fun allPads() = DsuRequest.PadData(flags = 0, slot = 0)
    private fun slotBased(slot: Int) = DsuRequest.PadData(flags = 1, slot = slot)

    @Test
    fun `an all-pads subscriber receives every slot`() {
        registry.register(client, allPads(), nowMillis = 0)

        assertEquals(listOf(client), registry.recipientsFor(slot = 0, nowMillis = 0))
        assertEquals(listOf(client), registry.recipientsFor(slot = 3, nowMillis = 0))
    }

    @Test
    fun `a slot-based subscriber only receives its slot`() {
        registry.register(client, slotBased(2), nowMillis = 0)

        assertEquals(listOf(client), registry.recipientsFor(slot = 2, nowMillis = 0))
        assertTrue(registry.recipientsFor(slot = 0, nowMillis = 0).isEmpty())
    }

    @Test
    fun `slot subscriptions accumulate per client`() {
        registry.register(client, slotBased(1), nowMillis = 0)
        registry.register(client, slotBased(3), nowMillis = 0)

        assertEquals(listOf(client), registry.recipientsFor(slot = 1, nowMillis = 0))
        assertEquals(listOf(client), registry.recipientsFor(slot = 3, nowMillis = 0))
        assertEquals(1, registry.size)
    }

    @Test
    fun `clients subscribe independently`() {
        registry.register(client, slotBased(0), nowMillis = 0)
        registry.register(other, slotBased(1), nowMillis = 0)

        assertEquals(listOf(client), registry.recipientsFor(slot = 0, nowMillis = 0))
        assertEquals(listOf(other), registry.recipientsFor(slot = 1, nowMillis = 0))
    }

    @Test
    fun `a registered client is live within the timeout`() {
        registry.register(client, allPads(), nowMillis = 0)

        assertEquals(listOf(client), registry.recipientsFor(slot = 0, nowMillis = 5_000))
    }

    @Test
    fun `a silent client is dropped after the timeout`() {
        registry.register(client, allPads(), nowMillis = 0)

        assertTrue(registry.recipientsFor(slot = 0, nowMillis = 5_001).isEmpty())
        assertEquals(0, registry.size)
    }

    @Test
    fun `re-registering refreshes the deadline`() {
        registry.register(client, allPads(), nowMillis = 0)
        registry.register(client, allPads(), nowMillis = 4_000)

        assertEquals(listOf(client), registry.recipientsFor(slot = 0, nowMillis = 8_000))
    }

    @Test
    fun `register reports new clients only once`() {
        assertTrue(registry.register(client, allPads(), nowMillis = 0))
        assertTrue(!registry.register(client, allPads(), nowMillis = 1))
    }

    @Test
    fun `clear drops everyone`() {
        registry.register(client, allPads(), nowMillis = 0)
        registry.clear()

        assertTrue(registry.recipientsFor(slot = 0, nowMillis = 0).isEmpty())
    }
}
