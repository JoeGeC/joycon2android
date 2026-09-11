package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerState

/**
 * Maps players onto the protocol's four slots. Player N streams on slot N-1, so P5-P8 get no
 * slot and are not served.
 *
 * A pad packet carries exactly one accelerometer and gyroscope, so a player holding two Joy-Cons
 * cannot report both hands on one slot: the second hand needs a slot of its own for an emulator
 * to read it (Dolphin's Nunchuk accelerometer, say). Players themselves always win the slot their
 * number gives them; pairs then take whatever is left, highest first, in player order. Four
 * players therefore leave nothing over and no pair gets a second hand.
 */
object DsuSlots {
    const val COUNT = 4

    fun streams(players: List<PlayerState>): List<DsuStream> = held(players) + secondHands(players)

    fun secondHands(players: List<PlayerState>): List<DsuStream> {
        val free = ((COUNT - 1) downTo 0) - held(players).map { it.slot }.toSet()
        return players.filter { it.hasFullController }
            .zip(free) { player, slot -> DsuStream(slot, PlayerState(player.player, left = player.left)) }
    }

    private fun held(players: List<PlayerState>): List<DsuStream> =
        players.filter { it.hasController }
            .mapNotNull { player -> slotFor(player)?.let { DsuStream(it, player) } }

    private fun slotFor(player: PlayerState): Int? =
        (player.player.index - 1).takeIf { it in 0 until COUNT }
}
