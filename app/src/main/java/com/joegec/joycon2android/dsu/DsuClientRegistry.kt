package com.joegec.joycon2android.dsu

import java.net.SocketAddress

/**
 * Clients subscribe by sending pad-data requests and are dropped after [timeoutMillis]
 * of silence (the spec's ~5 s liveness convention). Callers supply the clock so the
 * registry stays pure; register and live are called from different coroutines.
 */
class DsuClientRegistry(private val timeoutMillis: Long = 5_000) {

    private val lastSeen = mutableMapOf<SocketAddress, Long>()

    val size: Int get() = synchronized(lastSeen) { lastSeen.size }

    /** Returns true when this is a new client rather than a liveness refresh. */
    fun register(client: SocketAddress, nowMillis: Long): Boolean =
        synchronized(lastSeen) { lastSeen.put(client, nowMillis) == null }

    fun live(nowMillis: Long): List<SocketAddress> = synchronized(lastSeen) {
        lastSeen.entries.removeAll { (_, seen) -> nowMillis - seen > timeoutMillis }
        lastSeen.keys.toList()
    }

    fun clear() = synchronized(lastSeen) { lastSeen.clear() }
}
