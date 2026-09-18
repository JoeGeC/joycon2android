package com.joegec.joycon2android.update

import android.content.Context
import android.content.pm.PackageManager

fun installedAppVersion(context: Context): AppVersion? = try {
    AppVersion.parse(context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty())
} catch (_: PackageManager.NameNotFoundException) {
    null
}
