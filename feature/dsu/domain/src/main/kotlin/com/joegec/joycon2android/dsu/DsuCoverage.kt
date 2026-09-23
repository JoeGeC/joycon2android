package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerNumber

/** Reported apart: a player without a second slot still streams both hands' buttons, losing only left-hand motion. */
data class DsuCoverage(
    val unservedPlayers: List<PlayerNumber> = emptyList(),
    val unservedSecondHands: List<PlayerNumber> = emptyList(),
) {
    val allStreamed: Boolean get() = unservedPlayers.isEmpty() && unservedSecondHands.isEmpty()
}
