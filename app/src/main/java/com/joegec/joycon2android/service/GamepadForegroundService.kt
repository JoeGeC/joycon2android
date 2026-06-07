package com.joegec.joycon2android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.joegec.joycon2android.R

class GamepadForegroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        acquireWakeLock()
    }

    override fun onDestroy() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Gamepad Service",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Virtual gamepad is active"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Joy-Con Gamepad Active")
            .setContentText("Virtual gamepad input is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Joycon2Android::GamepadService",
        ).apply { acquire() }
    }

    companion object {
        const val CHANNEL_ID = "gamepad_service"
        const val NOTIFICATION_ID = 1
    }
}
