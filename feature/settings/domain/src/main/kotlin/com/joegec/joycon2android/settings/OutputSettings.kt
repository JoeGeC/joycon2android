package com.joegec.joycon2android.settings

data class OutputSettings(
    val fasterUpdates: Boolean = true,
    val blockDeviceMotion: Boolean = true,
    val mouse: Boolean = false,
    val keyboard: Boolean = false,
)
