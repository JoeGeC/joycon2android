package com.joegec.joycon2android.output

/** [emulators] keeps each game's own pick, e.g. Eden or Eden Nightly for [Playing.SWITCH]. */
data class PlayingSelection(
    val playing: Playing = Playing.ANDROID,
    val emulators: Map<Playing, String> = emptyMap(),
    val motion: Boolean = true,
    /** Only where [Playing.padOptional]; everywhere else the pad always runs. */
    val pad: Boolean = false,
) {
    val emulatorId: String? get() = emulators[playing]
}
