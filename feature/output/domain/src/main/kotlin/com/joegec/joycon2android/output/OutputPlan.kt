package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console

/** Which devices run and which mapping each reads: docs/architecture.md#output */
object OutputPlan {

    /** [mapped] is what the running tabs' mappings ask for: the keyboard for keys, the mouse for clicks, the pad for Motion's rumble. */
    fun devices(running: Set<OutputMode>, mapped: Set<OutputDevice>): Set<OutputDevice> =
        if (running.isEmpty()) emptySet() else running.flatMapTo(mutableSetOf()) { it.devices } + mapped

    /** With no emulator to set up, a tab maps only the app's own keys. */
    fun console(mode: OutputMode, emulator: EmulatorKind?): Console = when (mode) {
        OutputMode.GAMEPAD, OutputMode.MOTION -> emulator?.console ?: Console.ANDROID
        OutputMode.MOUSE_KEYBOARD -> Console.PC
    }

    /** DSU carries no rumble, so Motion's comes through the pad: docs/virtual-gamepad.md#rumble-over-dsu */
    fun motionRumblesThroughPad(console: Console, rumblePercents: Collection<Int>): Boolean =
        console in RUMBLE_THROUGH_PAD && rumblePercents.any { it > 0 }

    private val RUMBLE_THROUGH_PAD = setOf(Console.WIIMOTE_NUNCHUK, Console.SWITCH_PRO)

    /** Both tabs set up Eden's same player bindings, so only the one set up last reaches it. */
    fun setupsCollide(gamepad: EmulatorTarget?, motion: EmulatorTarget?): Boolean =
        gamepad?.kind == EmulatorKind.EDEN && gamepad.packageName == motion?.packageName
}
