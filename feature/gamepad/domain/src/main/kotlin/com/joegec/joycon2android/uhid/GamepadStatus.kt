package com.joegec.joycon2android.uhid

data class GamepadStatus(
    val enabled: Boolean = false,
    val error: String? = null,
)
