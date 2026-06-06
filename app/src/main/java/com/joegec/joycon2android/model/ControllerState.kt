package com.joegec.joycon2android.model

data class ControllerState(
    val scanning: Boolean = false,
    val error: String? = null,
    val left: JoyconConnectionState = JoyconConnectionState(),
    val right: JoyconConnectionState = JoyconConnectionState(),
    val leftInput: JoyconInput = JoyconInput(),
    val rightInput: JoyconInput = JoyconInput(),
) {
    val anyConnected: Boolean get() = left.connected || right.connected
    val bothConnected: Boolean get() = left.connected && right.connected
    val anyConnecting: Boolean get() = left.connecting || right.connecting

    val pressed: Set<String> get() = leftInput.pressed + rightInput.pressed
    val leftStickX: Int get() = leftInput.stickX
    val leftStickY: Int get() = leftInput.stickY
    val rightStickX: Int get() = rightInput.stickX
    val rightStickY: Int get() = rightInput.stickY
}
