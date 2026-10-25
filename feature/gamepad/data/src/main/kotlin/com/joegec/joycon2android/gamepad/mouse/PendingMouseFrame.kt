package com.joegec.joycon2android.gamepad.mouse

/** Sums movement between writes, so a slow write delays the pointer but never drops it. */
class PendingMouseFrame {

    private var pending = MouseFrame()
    private var sentButtons = emptySet<MouseButton>()

    /** True when [frame] leaves something to send. */
    @Synchronized
    fun add(frame: MouseFrame): Boolean {
        pending = MouseFrame(
            dx = pending.dx + frame.dx,
            dy = pending.dy + frame.dy,
            wheel = pending.wheel + frame.wheel,
            buttons = frame.buttons,
        )
        return hasNews()
    }

    @Synchronized
    fun take(): MouseFrame? {
        if (!hasNews()) return null
        val frame = pending
        sentButtons = frame.buttons
        pending = MouseFrame(buttons = frame.buttons)
        return frame
    }

    @Synchronized
    fun clear() {
        pending = MouseFrame()
        sentButtons = emptySet()
    }

    private fun hasNews() = pending.moves || pending.buttons != sentButtons
}
