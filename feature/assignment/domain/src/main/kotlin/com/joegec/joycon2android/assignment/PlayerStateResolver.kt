package com.joegec.joycon2android.assignment

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side

/** Fills a player's left/right slots, inferring sides the BLE advertisement didn't reveal. */
class PlayerStateResolver(private val evictConflicting: (address: String) -> Unit) {

    fun resolve(player: PlayerNumber, assigned: List<ConnectedJoycon>): PlayerState {
        val pro = assigned.find { it.side == Side.PRO }
        if (pro != null) {
            return PlayerState(player = player, left = pro, right = pro)
        }

        val knownLeft = assigned.find { it.side == Side.LEFT }
        val knownRight = assigned.find { it.side == Side.RIGHT }
        val unknowns = assigned.filter { it.side == Side.UNKNOWN }

        if (unknowns.isEmpty() || (knownLeft != null && knownRight != null)) {
            return PlayerState(player = player, left = knownLeft, right = knownRight ?: unknowns.firstOrNull())
        }

        if (unknowns.size == 1) {
            return resolveSingleUnknown(player, knownLeft, knownRight, unknowns.first())
        }

        return resolveTwoUnknowns(player, knownLeft, knownRight, unknowns[0], unknowns[1])
    }

    private fun resolveSingleUnknown(
        player: PlayerNumber,
        knownLeft: ConnectedJoycon?,
        knownRight: ConnectedJoycon?,
        joycon: ConnectedJoycon,
    ): PlayerState {
        if (knownLeft != null) return PlayerState(player = player, left = knownLeft, right = joycon)
        if (knownRight != null) return PlayerState(player = player, left = joycon, right = knownRight)

        return when (SideInference.inferSide(joycon.input)) {
            Side.LEFT -> PlayerState(player = player, left = joycon, right = null)
            else -> PlayerState(player = player, left = null, right = joycon)
        }
    }

    private fun resolveTwoUnknowns(
        player: PlayerNumber,
        knownLeft: ConnectedJoycon?,
        knownRight: ConnectedJoycon?,
        first: ConnectedJoycon,
        second: ConnectedJoycon,
    ): PlayerState {
        val firstSide = SideInference.inferSide(first.input)
        val secondSide = SideInference.inferSide(second.input)

        if (firstSide == secondSide && firstSide != Side.UNKNOWN) {
            evictConflicting(second.address)
            return when (firstSide) {
                Side.LEFT -> PlayerState(player = player, left = knownLeft ?: first, right = knownRight)
                else -> PlayerState(player = player, left = knownLeft, right = knownRight ?: first)
            }
        }

        val (left, right) = when {
            firstSide == Side.LEFT && secondSide != Side.LEFT -> first to second
            secondSide == Side.LEFT && firstSide != Side.LEFT -> second to first
            firstSide == Side.RIGHT && secondSide != Side.RIGHT -> second to first
            secondSide == Side.RIGHT && firstSide != Side.RIGHT -> first to second
            else -> first to second // can't distinguish yet; arbitrary
        }

        return PlayerState(
            player = player,
            left = knownLeft ?: left,
            right = knownRight ?: right,
        )
    }
}
