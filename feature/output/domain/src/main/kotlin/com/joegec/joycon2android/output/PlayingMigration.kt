package com.joegec.joycon2android.output

/** From the per-device tabs to one game: docs/architecture.md#output */
object PlayingMigration {

    /**
     * [remembered] is the tabs left on, null if never stored; [targetOf] the emulator a tab had.
     * The first match wins, in the order below.
     */
    fun selection(remembered: Set<OutputMode>?, inView: OutputMode, targetOf: (OutputMode) -> EmulatorTarget?): PlayingSelection {
        val tabs = remembered?.takeIf { it.isNotEmpty() } ?: setOf(inView)
        fun on(mode: OutputMode, kind: EmulatorKind) = mode in tabs && targetOf(mode)?.kind == kind
        val playing = when {
            OutputMode.MOUSE_KEYBOARD in tabs -> Playing.PC
            on(OutputMode.MOTION, EmulatorKind.EDEN) || on(OutputMode.GAMEPAD, EmulatorKind.EDEN) -> Playing.SWITCH
            on(OutputMode.MOTION, EmulatorKind.DOLPHIN_WII) -> Playing.WII
            on(OutputMode.MOTION, EmulatorKind.OTHER) -> Playing.OTHER_EMULATOR
            on(OutputMode.GAMEPAD, EmulatorKind.DOLPHIN_GAMECUBE) -> Playing.GAMECUBE
            else -> Playing.ANDROID
        }
        val eden = listOf(OutputMode.MOTION, OutputMode.GAMEPAD).firstNotNullOfOrNull { mode ->
            targetOf(mode)?.takeIf { it.kind == EmulatorKind.EDEN }?.id
        }
        return PlayingSelection(
            playing = playing,
            emulators = listOfNotNull(eden?.let { Playing.SWITCH to it }).toMap(),
            motion = playing != Playing.SWITCH || on(OutputMode.MOTION, EmulatorKind.EDEN),
        )
    }
}
