package com.joegec.joycon2android.emulator

import android.content.Context
import android.content.Intent
import android.util.Log

/** Starts an installed emulator so it reloads the config auto setup just wrote. */
class EmulatorLauncher(context: Context) {

    private val appContext = context.applicationContext

    fun launch(packageName: String) {
        val intent = appContext.packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            Log.w(TAG, "no launch intent for $packageName")
            return
        }
        appContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private companion object {
        const val TAG = "EmulatorLauncher"
    }
}
