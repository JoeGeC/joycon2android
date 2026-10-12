package com.joegec.joycon2android.connection

/** How long a dropped controller keeps its place: docs/protocol.md#coming-back-after-a-drop */
internal class ReturnWindow(
    private val windowMs: Long,
    private val scheduler: OpScheduler,
    private val onExpired: () -> Unit,
) {
    @Volatile var isOpen = false
        private set

    private val expiry = Runnable {
        if (!isOpen) return@Runnable
        isOpen = false
        onExpired()
    }

    /** A drop while already open keeps the original deadline. */
    fun open() {
        if (isOpen) return
        isOpen = true
        scheduler.postDelayed(expiry, windowMs)
    }

    fun close() {
        isOpen = false
        scheduler.cancel(expiry)
    }
}
