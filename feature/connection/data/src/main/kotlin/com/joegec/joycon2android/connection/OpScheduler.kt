package com.joegec.joycon2android.connection

internal interface OpScheduler {
    fun post(task: Runnable)
    fun postDelayed(task: Runnable, delayMs: Long)
    fun cancel(task: Runnable)
}
