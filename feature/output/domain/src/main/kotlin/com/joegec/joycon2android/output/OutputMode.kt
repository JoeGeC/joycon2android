package com.joegec.joycon2android.output

/** One tab of the Output card, each the device it is named for; Mouse & keys runs two. */
enum class OutputMode(val devices: Set<OutputDevice>) {
    GAMEPAD(setOf(OutputDevice.PAD)),
    MOTION(setOf(OutputDevice.MOTION)),
    MOUSE_KEYBOARD(setOf(OutputDevice.KEYBOARD, OutputDevice.MOUSE)),
}
