package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerNumber

/**
 * What the four slots cannot carry. A pad packet holds one accelerometer and gyroscope, so a
 * player without a second slot still streams both Joy-Cons' buttons — only the left hand's
 * motion is lost, which is why the two shortfalls are reported apart.
 */
data class DsuCoverage(
    val unservedPlayers: List<PlayerNumber> = emptyList(),
    val unservedSecondHands: List<PlayerNumber> = emptyList(),
) {
    val allStreamed: Boolean get() = unservedPlayers.isEmpty() && unservedSecondHands.isEmpty()
}
