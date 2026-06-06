package com.joegec.joycon2android.model

/** Per-controller raw input from a single Joy-Con 2. */
data class JoyconInput(
    val packetId: Int = 0,
    val buttons: Long = 0,
    val pressed: Set<String> = emptySet(),
    val stickX: Int = 2048,
    val stickY: Int = 2048,
    val accelX: Int = 0,
    val accelY: Int = 0,
    val accelZ: Int = 0,
    val gyroX: Int = 0,
    val gyroY: Int = 0,
    val gyroZ: Int = 0,
    val batteryVolts: Float = 0f,
)

/** Connection state for a single Joy-Con. */
data class JoyconConnectionState(
    val connected: Boolean = false,
    val connecting: Boolean = false,
    val deviceName: String? = null,
    val error: String? = null,
)

/** Combined state for the UI, merging left + right Joy-Con data. */
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
