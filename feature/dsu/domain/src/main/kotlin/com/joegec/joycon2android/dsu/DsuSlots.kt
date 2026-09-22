package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerState

/**
 * Maps players onto the protocol's four slots, and a pair's second hand onto a slot of its own
 * since one packet carries one IMU: docs/dsu-motion.md#slots.
 */
object DsuSlots {
    const val COUNT = 4

    fun streams(players: List<PlayerState>): List<DsuStream> = held(players) + secondHands(players)

    fun secondHands(players: List<PlayerState>): List<DsuStream> {
        val slotted = held(players)
        val free = ((COUNT - 1) downTo 0) - slotted.map { it.slot }.toSet()
        return slotted.map { it.state }
            .filter { it.hasFullController }
            // Alone on its slot, but still the upright half of a pair.
            .zip(free) { player, slot ->
                DsuStream(slot, PlayerState(player.player, left = player.left), heldSideways = false)
            }
    }

    fun coverage(players: List<PlayerState>): DsuCoverage {
        val slotted = held(players).map { it.state }
        val secondHanded = secondHands(players).map { it.state.player }.toSet()
        return DsuCoverage(
            unservedPlayers = players.filter { it.hasController }.map { it.player } - slotted.map { it.player }.toSet(),
            unservedSecondHands = slotted.filter { it.hasFullController }.map { it.player } - secondHanded,
        )
    }

    private fun held(players: List<PlayerState>): List<DsuStream> =
        players.filter { it.hasController }
            .mapNotNull { player -> slotFor(player)?.let { DsuStream(it, player) } }

    private fun slotFor(player: PlayerState): Int? =
        (player.player.index - 1).takeIf { it in 0 until COUNT }
}
