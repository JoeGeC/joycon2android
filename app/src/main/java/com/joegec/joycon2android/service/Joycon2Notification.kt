package com.joegec.joycon2android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.joegec.joycon2android.R

class Joycon2Notification(private val context: Context) {

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun build(reason: ForegroundReason): Notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(reason.title()))
        .setContentText(context.getString(reason.text()))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)
        .setContentIntent(openAppIntent(context))
        .addAction(
            0,
            context.getString(R.string.notification_action_disconnect_all),
            disconnectAllIntent(),
        )
        .build()

    private fun disconnectAllIntent(): PendingIntent {
        val intent = Intent(context, Joycon2Service::class.java).apply {
            action = Joycon2Service.ACTION_DISCONNECT_ALL
        }
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun ForegroundReason.title() = when (this) {
        ForegroundReason.CONNECTED -> R.string.notification_title
        ForegroundReason.SCANNING -> R.string.notification_scanning_title
    }

    private fun ForegroundReason.text() = when (this) {
        ForegroundReason.CONNECTED -> R.string.notification_text
        ForegroundReason.SCANNING -> R.string.notification_scanning_text
    }

    companion object {
        const val ID = 1
        private const val CHANNEL_ID = "joycon2_service"
    }
}
