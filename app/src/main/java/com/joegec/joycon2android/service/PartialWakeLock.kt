package com.joegec.joycon2android.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager

/** Holds a partial wake lock so BLE input keeps flowing while the screen is off. */
class PartialWakeLock(context: Context, private val tag: String) {

    private val powerManager = context.getSystemService(PowerManager::class.java)
    private var wakeLock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout") // intentionally held for the service's full lifetime
    fun acquire() {
        if (wakeLock != null) return
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag).apply { acquire() }
    }

    fun release() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }
}
