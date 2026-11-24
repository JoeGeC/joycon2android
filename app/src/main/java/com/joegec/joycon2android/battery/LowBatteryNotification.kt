package com.joegec.joycon2android.battery

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.service.openAppIntent

class LowBatteryNotification(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.low_battery_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.low_battery_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun show(battery: ControllerBattery) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        if (!manager.areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.low_battery_title))
            .setContentText(text(battery))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()
        manager.notify(battery.address, ID, notification)
    }

    private fun text(battery: ControllerBattery): String {
        val controller = controllerName(battery)
        val player = battery.player
            ?: return context.getString(R.string.low_battery_text_unassigned, controller, battery.percent)
        return context.getString(R.string.low_battery_text, player.index, controller, battery.percent)
    }

    private fun controllerName(battery: ControllerBattery): String = when (battery.model) {
        ControllerModel.JOYCON_LEFT -> context.getString(R.string.low_battery_joycon_left)
        ControllerModel.JOYCON_RIGHT -> context.getString(R.string.low_battery_joycon_right)
        ControllerModel.PRO_CONTROLLER -> context.getString(R.string.low_battery_pro)
        ControllerModel.GAMECUBE -> context.getString(R.string.low_battery_gamecube)
        ControllerModel.UNKNOWN -> battery.deviceName
    }

    private companion object {
        const val ID = 2
        const val CHANNEL_ID = "low_battery"
    }
}
