package com.joegec.joycon2android.connectionloss

import android.content.Context
import androidx.core.app.NotificationCompat
import com.joegec.joycon2android.R
import com.joegec.joycon2android.notification.AlertPoster
import com.joegec.joycon2android.notification.ControllerNames
import com.joegec.joycon2android.service.openAppIntent

class ConnectionLossNotification(
    private val context: Context,
    private val poster: AlertPoster,
    private val names: ControllerNames,
) {

    fun createChannel() = poster.createChannel(
        CHANNEL_ID,
        context.getString(R.string.notification_connection_lost_channel_name),
        context.getString(R.string.notification_connection_lost_channel_description),
    )

    fun show(controller: LostController) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(title(controller)))
            .setContentText(text(controller))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()
        poster.post(controller.address, ID, notification)
    }

    fun clear(address: String) = poster.cancel(address, ID)

    private fun title(controller: LostController): Int =
        if (controller.ranOutOfBattery) R.string.notification_battery_flat_title
        else R.string.notification_connection_lost_title

    private fun text(controller: LostController): String {
        val name = names.name(controller.model, controller.deviceName)
        val player = controller.player
        return when {
            controller.ranOutOfBattery && player != null ->
                context.getString(R.string.notification_battery_flat_text, player.index, name)
            controller.ranOutOfBattery -> context.getString(R.string.notification_battery_flat_text_unassigned, name)
            player != null -> context.getString(R.string.notification_connection_lost_text, player.index, name)
            else -> context.getString(R.string.notification_connection_lost_text_unassigned, name)
        }
    }

    private companion object {
        const val ID = 3
        const val CHANNEL_ID = "connection_lost"
    }
}
