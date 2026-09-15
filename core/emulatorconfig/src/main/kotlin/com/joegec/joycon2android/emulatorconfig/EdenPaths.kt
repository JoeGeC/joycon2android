package com.joegec.joycon2android.emulatorconfig

/**
 * Eden's packages and config-file location on Android. Shared because both the DSU feature
 * (cemuhook motion input) and the Virtual Gamepad feature (Pro Controller mapping) write to the
 * same `config.ini` — writable by a shell-uid process (Shizuku / wireless debugging), not by us.
 *
 * Stable and nightly install side by side under different package names, so a path is always
 * derived from the package the user picked rather than assumed.
 */
object EdenPaths {
    const val PACKAGE = "dev.eden.eden_emulator"
    const val NIGHTLY_PACKAGE = "dev.eden.eden_emulator.nightly"
    val PACKAGES = setOf(PACKAGE, NIGHTLY_PACKAGE)

    fun config(packageName: String) = "/sdcard/Android/data/$packageName/files/config/config.ini"
}
