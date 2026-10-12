package com.joegec.joycon2android.connection

import android.os.Handler
import android.os.Looper

internal class MainThreadScheduler : OpScheduler {

    private val handler = Handler(Looper.getMainLooper())

    override fun post(task: Runnable) {
        handler.post(task)
    }

    override fun postDelayed(task: Runnable, delayMs: Long) {
        handler.postDelayed(task, delayMs)
    }

    override fun cancel(task: Runnable) {
        handler.removeCallbacks(task)
    }
}
