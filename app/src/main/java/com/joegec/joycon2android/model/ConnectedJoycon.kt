package com.joegec.joycon2android.model

data class ConnectedJoycon(
    val address: String,
    val side: Side,
    val deviceName: String,
    val connectionState: JoyconConnectionState = JoyconConnectionState(),
    val input: JoyconInput = JoyconInput(),
    val assignedPlayer: PlayerNumber? = null,
    val ready: Boolean = false,
) {
    /** Shell accent color (0xRRGGBB) read from the controller's SPI flash, or null if not yet known. */
    val accentColor: Int? get() = connectionState.accentColor
}
