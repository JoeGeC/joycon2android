package com.joegec.joycon2android.connection

internal class ManualScheduler : OpScheduler {

    private data class Task(val runnable: Runnable, val dueMs: Long)

    private val tasks = mutableListOf<Task>()
    private var nowMs = 0L

    override fun post(task: Runnable) {
        postDelayed(task, 0L)
    }

    override fun postDelayed(task: Runnable, delayMs: Long) {
        tasks += Task(task, nowMs + delayMs)
    }

    override fun cancel(task: Runnable) {
        tasks.removeAll { it.runnable === task }
    }

    fun runDue() {
        while (true) {
            val next = tasks.filter { it.dueMs <= nowMs }.minByOrNull { it.dueMs } ?: return
            tasks.remove(next)
            next.runnable.run()
        }
    }

    fun advanceBy(ms: Long) {
        nowMs += ms
        runDue()
    }
}
