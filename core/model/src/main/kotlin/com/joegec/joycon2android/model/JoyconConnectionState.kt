package com.joegec.joycon2android.model

data class JoyconConnectionState(
    val connected: Boolean = false,
    val connecting: Boolean = false,
    val ready: Boolean = false,
    /** Dropped, and held for a while in case it comes back. */
    val connectionLost: Boolean = false,
    val deviceName: String? = null,
    val error: String? = null,
    /** 0xRRGGBB from SPI flash; null until read, or unset on the controller. */
    val accentColor: Int? = null,
)
