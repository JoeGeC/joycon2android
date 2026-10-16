package com.joegec.joycon2android.emulator

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.Display

class EmulatorLauncher(context: Context) {

    private val appContext = context.applicationContext

    fun launch(packageName: String, onMainScreen: Boolean) {
        val intent = appContext.packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            Log.w(TAG, "no launch intent for $packageName")
            return
        }
        appContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), launchOptions(onMainScreen))
    }

    private fun launchOptions(onMainScreen: Boolean) =
        if (onMainScreen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityOptions.makeBasic().setLaunchDisplayId(Display.DEFAULT_DISPLAY).toBundle()
        } else {
            null
        }

    private companion object {
        const val TAG = "EmulatorLauncher"
    }
}
