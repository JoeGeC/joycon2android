package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console

/** Which devices run and which mapping they read: docs/architecture.md#output */
object OutputPlan {

    /** [mapped] is what the active mapping presses: the keyboard for its keys, the mouse for clicks. */
    fun devices(mode: OutputMode, mapped: Set<OutputDevice>): Set<OutputDevice> = mode.devices + mapped

    /** With no emulator to set up, a tab maps only the app's own keys. */
    fun console(mode: OutputMode, emulator: EmulatorKind?): Console = when (mode) {
        OutputMode.GAMEPAD, OutputMode.MOTION -> emulator?.console ?: Console.ANDROID
        OutputMode.MOUSE_KEYBOARD -> Console.PC
    }
}
