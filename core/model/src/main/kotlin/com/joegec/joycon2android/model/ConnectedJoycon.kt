package com.joegec.joycon2android.model

data class ConnectedJoycon(
    val address: String,
    val side: Side,
    val deviceName: String,
    val model: ControllerModel = ControllerModel.UNKNOWN,
    val detectedModel: ControllerModel = model,
    val typeOverride: ControllerModel? = null,
    val connectionState: JoyconConnectionState = JoyconConnectionState(),
    val input: JoyconInput = JoyconInput(),
    val assignedPlayer: PlayerNumber? = null,
    val ready: Boolean = false,
) {
    val accentColor: Int? get() = connectionState.accentColor
    val connectionLost: Boolean get() = connectionState.connectionLost

    /** Keeps what a packet rarely changes, so the result is equal from one packet to the next. */
    fun withoutLiveInput(): ConnectedJoycon =
        copy(input = JoyconInput(battery = input.battery, motionSupport = input.motionSupport))

    fun withLiveInput(inputs: Map<String, JoyconInput>): ConnectedJoycon = copy(input = inputs[address] ?: input)
}
