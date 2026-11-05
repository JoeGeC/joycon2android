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

    /**
     * The devices [playing] runs: what the game reads, the pad for getting round the device, and
     * whatever [mapped] keys and clicks ask for. Without Shizuku only DSU can run, so a game that
     * reads buttons there gets them over DSU instead.
     */
    fun devices(playing: Playing, motion: Boolean, shizuku: Boolean, mapped: Set<OutputDevice>): Set<OutputDevice> {
        val game = if (playing.motionOptional && !motion) playing.needs - OutputDevice.MOTION else playing.needs
        val wanted = game + OutputDevice.PAD + mapped
        if (shizuku) return wanted
        return wanted.filterTo(mutableSetOf()) { it !in NEEDS_SHIZUKU } +
            if (playing.buttonsOverDsu) setOf(OutputDevice.MOTION) else emptySet()
    }

    private val NEEDS_SHIZUKU = setOf(OutputDevice.PAD, OutputDevice.KEYBOARD, OutputDevice.MOUSE)

    private val RUMBLE_THROUGH_PAD = setOf(Console.WIIMOTE_NUNCHUK, Console.SWITCH_PRO)
}
