package com.joegec.joycon2android.output

/** The tab, and the emulator each tab last had, so switching tabs keeps both choices. */
data class OutputSelection(
    val mode: OutputMode = OutputMode.GAMEPAD,
    val emulators: Map<OutputMode, String> = emptyMap(),
) {
    val emulatorId: String? get() = emulators[mode]
}
