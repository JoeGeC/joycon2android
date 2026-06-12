package com.joegec.joycon2android.adb

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

/**
 * Opens the system Wireless Debugging screen. There's no public Settings action for it,
 * so try the AOSP screen directly, then Developer Options with the toggle highlighted,
 * then plain Developer Options — using the first that resolves.
 */
object WirelessDebuggingSettings {

    private const val HIGHLIGHT_KEY = ":settings:fragment_args_key"
    private const val SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args"
    private const val WIRELESS_DEBUG_PREF = "toggle_adb_wireless"

    fun open(context: Context) {
        val highlight = Bundle().apply { putString(HIGHLIGHT_KEY, WIRELESS_DEBUG_PREF) }
        val candidates = listOf(
            Intent().setClassName(
                "com.android.settings",
                "com.android.settings.Settings\$AdbWirelessSettingsActivity",
            ),
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                .putExtra(HIGHLIGHT_KEY, WIRELESS_DEBUG_PREF)
                .putExtra(SHOW_FRAGMENT_ARGS, highlight),
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
        )
        for (intent in candidates) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: ActivityNotFoundException) {
            } catch (_: SecurityException) {
            }
        }
    }
}
