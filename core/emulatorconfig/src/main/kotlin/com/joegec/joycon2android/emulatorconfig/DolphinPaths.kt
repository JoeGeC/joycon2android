package com.joegec.joycon2android.emulatorconfig

/**
 * Dolphin's package and config-file locations on Android. Shared because both the DSU feature
 * (motion input) and the Virtual Gamepad feature (GameCube pad mapping) write to the same app's
 * external config dir — writable by a shell-uid process (Shizuku / wireless debugging), not by us.
 */
object DolphinPaths {
    const val PACKAGE = "org.dolphinemu.dolphinemu"

    fun config(file: String) = "/sdcard/Android/data/$PACKAGE/files/Config/$file"
}
