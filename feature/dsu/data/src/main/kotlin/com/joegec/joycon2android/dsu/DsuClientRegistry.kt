package com.joegec.joycon2android.dsu

import java.net.SocketAddress

/**
 * Tracks pad-data subscribers and which slots each one wants. Routing per slot on the
 * server side matters: Dolphin's DSU devices overwrite their pad state with every
 * received packet without checking the slot, so a server that broadcasts all slots to
 * every client makes the last controller win. Clients are dropped after
 * [timeoutMillis] of silence (the spec's ~5 s liveness convention). Callers supply the
 * clock so the registry stays pure; register and recipientsFor run on different
 * coroutines.
 */
class DsuClientRegistry(private val timeoutMillis: Long = 5_000) {

    private class Subscription {
        var lastSeen = 0L
        var allSlots = false
        val slots = mutableSetOf<Int>()
    }

    private val subscriptions = mutableMapOf<SocketAddress, Subscription>()

    val size: Int get() = synchronized(subscriptions) { subscriptions.size }

    /** Returns true when this is a new client rather than a liveness refresh. */
    fun register(client: SocketAddress, request: DsuRequest.PadData, nowMillis: Long): Boolean =
        synchronized(subscriptions) {
            val isNew = client !in subscriptions
            val subscription = subscriptions.getOrPut(client) { Subscription() }
            subscription.lastSeen = nowMillis
            // Slot registrations accumulate (one Dolphin socket per slot). All-pads and
            // MAC-based registrations get every slot — MAC registrants filter client-side.
            if (request.flags and FLAG_SLOT_BASED != 0) {
                subscription.slots += request.slot
            } else {
                subscription.allSlots = true
            }
            isNew
        }

    fun recipientsFor(slot: Int, nowMillis: Long): List<SocketAddress> =
        synchronized(subscriptions) {
            subscriptions.entries.removeAll { (_, sub) -> nowMillis - sub.lastSeen > timeoutMillis }
            subscriptions.filterValues { it.allSlots || slot in it.slots }.keys.toList()
        }

    fun clear() = synchronized(subscriptions) { subscriptions.clear() }

    companion object {
        private const val FLAG_SLOT_BASED = 0x01
    }
}
