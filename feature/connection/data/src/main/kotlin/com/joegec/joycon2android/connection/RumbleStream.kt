package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Rumble
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/** Resends a rumble as each packet runs out, then sends one stop: docs/protocol.md#hd-rumble */
internal class RumbleStream(private val send: (Rumble) -> Unit) {

    private var ticks: ScheduledFuture<*>? = null

    @Volatile
    private var current = Rumble.OFF

    @Synchronized
    fun set(rumble: Rumble) {
        current = rumble
        if (rumble.isOn && ticks == null) {
            ticks = SCHEDULER.scheduleAtFixedRate({ send(current) }, 0, FRAME_MS, TimeUnit.MILLISECONDS)
        } else if (!rumble.isOn && ticks != null) {
            ticks?.cancel(false)
            ticks = null
            SCHEDULER.execute { send(Rumble.OFF) }
        }
    }

    private companion object {
        const val FRAME_MS = 12L

        val SCHEDULER = Executors.newSingleThreadScheduledExecutor { Thread(it, "rumble").apply { isDaemon = true } }
    }
}
