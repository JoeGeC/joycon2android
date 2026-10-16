package com.joegec.joycon2android.emulator

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display

class DeviceScreens(context: Context) {

    private val displayManager = context.applicationContext.getSystemService(DisplayManager::class.java)

    /** Launching onto a chosen display needs API 26. */
    fun canLaunchOnMainScreen(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            displayManager.displays.count { it.flags and Display.FLAG_PRIVATE == 0 } > 1
}
