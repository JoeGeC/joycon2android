package com.joegec.joycon2android.widget

import com.joegec.joycon2android.ble.ScanBlocker
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.output.PlayingSelection

data class HomeWidgetState(
    val scanning: Boolean = false,
    val players: List<WidgetPlayer> = emptyList(),
    /** Connected but not yet assigned a player. */
    val waiting: Int = 0,
    val playing: Playing = PlayingSelection().playing,
    /** Null where the game has no emulator to choose. */
    val emulatorName: String? = null,
    /** Null with nobody playing. */
    val layout: GlobalMapping? = null,
    val needsShizuku: Boolean = false,
    val blocker: ScanBlocker? = null,
)
