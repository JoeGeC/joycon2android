package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console

/** Which devices run and which mapping each reads: docs/architecture.md#output */
object OutputPlan {

    /** [mapped] is what the running tabs' mappings press: the keyboard for their keys, the mouse for clicks. */
    fun devices(running: Set<OutputMode>, mapped: Set<OutputDevice>): Set<OutputDevice> =
        if (running.isEmpty()) emptySet() else running.flatMapTo(mutableSetOf()) { it.devices } + mapped

    /** With no emulator to set up, a tab maps only the app's own keys. */
    fun console(mode: OutputMode, emulator: EmulatorKind?): Console = when (mode) {
        OutputMode.GAMEPAD, OutputMode.MOTION -> emulator?.console ?: Console.ANDROID
        OutputMode.MOUSE_KEYBOARD -> Console.PC
    }

    /** Both tabs set up Eden's same player bindings, so only the one set up last reaches it. */
    fun setupsCollide(gamepad: EmulatorTarget?, motion: EmulatorTarget?): Boolean =
        gamepad?.kind == EmulatorKind.EDEN && gamepad.packageName == motion?.packageName
}
