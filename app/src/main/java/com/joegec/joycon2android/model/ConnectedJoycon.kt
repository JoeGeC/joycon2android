package com.joegec.joycon2android.model

data class ConnectedJoycon(
    val address: String,
    val side: Side,
    val deviceName: String,
    val connectionState: JoyconConnectionState = JoyconConnectionState(),
    val input: JoyconInput = JoyconInput(),
    val assignedPlayer: PlayerNumber? = null,
)
