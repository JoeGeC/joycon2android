package com.joegec.joycon2android.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Posts per-controller alerts, tagged by address, once notifications are allowed. */
class AlertPoster(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    fun createChannel(id: String, name: String, description: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
            this.description = description
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun post(address: String, id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        if (!manager.areNotificationsEnabled()) return
        manager.notify(address, id, notification)
    }

    fun cancel(address: String, id: Int) = manager.cancel(address, id)
}
