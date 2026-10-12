package com.joegec.joycon2android.power

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

class BatteryOptimizationExemption(private val context: Context) {

    private val powerManager = context.getSystemService(PowerManager::class.java)

    fun isGranted(): Boolean = powerManager.isIgnoringBatteryOptimizations(context.packageName)

    // Play restricts this request to listed app types; the app ships from GitHub releases.
    @SuppressLint("BatteryLife")
    fun buildRequestIntent(): Intent {
        val request = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            .setData(Uri.fromParts("package", context.packageName, null))
        val settingsList = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", context.packageName, null))
        return listOf(request, settingsList).firstOrNull {
            it.resolveActivity(context.packageManager) != null
        } ?: appDetails
    }
}
