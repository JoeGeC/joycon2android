package com.joegec.joycon2android.emulatorconfig

/** Stable and nightly install side by side, so a path derives from the package the user picked. */
object EdenPaths {
    const val PACKAGE = "dev.eden.eden_emulator"
    const val NIGHTLY_PACKAGE = "dev.eden.eden_emulator.nightly"
    val PACKAGES = setOf(PACKAGE, NIGHTLY_PACKAGE)

    fun config(packageName: String) = "/sdcard/Android/data/$packageName/files/config/config.ini"
}
