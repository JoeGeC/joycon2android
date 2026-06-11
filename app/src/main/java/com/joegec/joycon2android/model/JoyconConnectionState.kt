package com.joegec.joycon2android.model

data class JoyconConnectionState(
    val connected: Boolean = false,
    val connecting: Boolean = false,
    val ready: Boolean = false,
    val deviceName: String? = null,
    val error: String? = null,
    /** Shell accent color read from SPI flash, packed as 0xRRGGBB. Null until read (or unset on the controller). */
    val accentColor: Int? = null,
)
