package com.joegec.joycon2android.gamepad.wirelessdebug

data class WirelessDebugStatus(
    val state: AdbState = AdbState.DISCONNECTED,
    val error: String? = null,
    val pairingServiceAvailable: Boolean = false,
    val shizukuAvailable: Boolean = false,
)
