package com.joegec.joycon2android.dsu

import java.net.InetSocketAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DsuClientRegistryTest {

    private val registry = DsuClientRegistry(timeoutMillis = 5_000)
    private val client = InetSocketAddress("127.0.0.1", 50000)

    @Test
    fun `a registered client is live within the timeout`() {
        registry.register(client, nowMillis = 0)

        assertEquals(listOf(client), registry.live(nowMillis = 5_000))
    }

    @Test
    fun `a silent client is dropped after the timeout`() {
        registry.register(client, nowMillis = 0)

        assertTrue(registry.live(nowMillis = 5_001).isEmpty())
        assertEquals(0, registry.size)
    }

    @Test
    fun `re-registering refreshes the deadline`() {
        registry.register(client, nowMillis = 0)
        registry.register(client, nowMillis = 4_000)

        assertEquals(listOf(client), registry.live(nowMillis = 8_000))
    }

    @Test
    fun `clients register once per address`() {
        registry.register(client, nowMillis = 0)
        registry.register(client, nowMillis = 1)

        assertEquals(1, registry.size)
    }

    @Test
    fun `clear drops everyone`() {
        registry.register(client, nowMillis = 0)
        registry.clear()

        assertTrue(registry.live(nowMillis = 0).isEmpty())
    }
}
