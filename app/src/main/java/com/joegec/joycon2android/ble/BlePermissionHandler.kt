package com.joegec.joycon2android.ble

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

class BlePermissionHandler(private val context: Context) {

    val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }

    fun isGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun buildSettingsIntent(): Intent {
        val nearbyIntent = Intent("android.settings.MANAGE_APP_PERMISSION").apply {
            putExtra("android.intent.extra.PACKAGE_NAME", context.packageName)
            putExtra(
                "android.intent.extra.PERMISSION_GROUP_NAME",
                "android.permission-group.NEARBY_DEVICES"
            )
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            nearbyIntent.resolveActivity(context.packageManager) != null
        ) {
            nearbyIntent
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
    }
}
