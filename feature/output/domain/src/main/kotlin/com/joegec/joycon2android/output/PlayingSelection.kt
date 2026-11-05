package com.joegec.joycon2android.output

/** [emulators] keeps each game's own pick, e.g. Eden or Eden Nightly for [Playing.SWITCH]. */
data class PlayingSelection(
    val playing: Playing = Playing.ANDROID,
    val emulators: Map<Playing, String> = emptyMap(),
    val motion: Boolean = true,
) {
    val emulatorId: String? get() = emulators[playing]
}
