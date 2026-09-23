package com.joegec.joycon2android.emulatorconfig

object DolphinPaths {
    const val PACKAGE = "org.dolphinemu.dolphinemu"

    fun config(file: String) = "/sdcard/Android/data/$PACKAGE/files/Config/$file"
}
