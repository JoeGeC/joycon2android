package com.joegec.joycon2android.battery

import android.content.Context
import androidx.core.app.NotificationCompat
import com.joegec.joycon2android.R
import com.joegec.joycon2android.notification.AlertPoster
import com.joegec.joycon2android.notification.ControllerNames
import com.joegec.joycon2android.service.openAppIntent

class LowBatteryNotification(
    private val context: Context,
    private val poster: AlertPoster,
    private val names: ControllerNames,
) {

    fun createChannel() = poster.createChannel(
        CHANNEL_ID,
        context.getString(R.string.low_battery_channel_name),
        context.getString(R.string.low_battery_channel_description),
    )

    fun show(battery: ControllerBattery) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.low_battery_title))
            .setContentText(text(battery))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()
        poster.post(battery.address, ID, notification)
    }

    private fun text(battery: ControllerBattery): String {
        val controller = names.name(battery.model, battery.deviceName)
        val player = battery.player
            ?: return context.getString(R.string.low_battery_text_unassigned, controller, battery.percent)
        return context.getString(R.string.low_battery_text, player.index, controller, battery.percent)
    }

    private companion object {
        const val ID = 2
        const val CHANNEL_ID = "low_battery"
    }
}
