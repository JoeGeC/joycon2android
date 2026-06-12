package com.joegec.joycon2android.adb

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.joegec.joycon2android.R

/**
 * Posts the "enter pairing code" notification while the system pairing dialog is open.
 * The code must be entered here rather than in the app because the pairing service only
 * lives while the user is on the Settings dialog — switching to the app would close it.
 */
class AdbPairingNotification(private val context: Context) {

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.adb_pairing_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission") // POST_NOTIFICATIONS requested before the ADB path is offered
    fun show() {
        val remoteInput = RemoteInput.Builder(KEY_CODE)
            .setLabel(context.getString(R.string.adb_pairing_code_label))
            .build()
        val replyIntent = Intent(context, AdbPairingReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val pending = PendingIntent.getBroadcast(context, 0, replyIntent, flags)
        val action = NotificationCompat.Action.Builder(
            0,
            context.getString(R.string.adb_pairing_notif_action),
            pending,
        ).addRemoteInput(remoteInput).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.adb_pairing_notif_title))
            .setContentText(context.getString(R.string.adb_pairing_notif_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(action)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancel() = NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

    companion object {
        const val KEY_CODE = "pairing_code"
        private const val CHANNEL_ID = "adb_pairing"
        private const val NOTIFICATION_ID = 4201
    }
}
