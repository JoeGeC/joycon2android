package com.joegec.joycon2android.output

data class OutputSelection(
    val mode: OutputMode = OutputMode.CONTROLLER,
    val emulatorId: String? = null,
    val overrides: Map<OutputDevice, DeviceOverride> = emptyMap(),
) {
    fun override(device: OutputDevice): DeviceOverride = overrides[device] ?: DeviceOverride.AUTO
}
