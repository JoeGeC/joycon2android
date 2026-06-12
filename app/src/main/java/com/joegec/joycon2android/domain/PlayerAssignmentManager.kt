package com.joegec.joycon2android.domain

import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Maps Joy-Con BLE addresses to player numbers.
 * Enforces that each player can have at most one Left and one Right controller.
 */
class PlayerAssignmentManager {

    private val _assignments = MutableStateFlow<Map<String, PlayerNumber>>(emptyMap())
    val assignments: StateFlow<Map<String, PlayerNumber>> = _assignments.asStateFlow()

    private val sides = mutableMapOf<String, Side>()

    fun assign(address: String, side: Side, player: PlayerNumber): Boolean {
        if (isSlotTaken(side, player)) return false
        sides[address] = side
        _assignments.value += (address to player)
        return true
    }

    fun unassign(address: String) {
        sides.remove(address)
        _assignments.value -= address
    }

    fun unassignAll() {
        sides.clear()
        _assignments.value = emptyMap()
    }

    fun getPlayer(address: String): PlayerNumber? = _assignments.value[address]

    /** Lowest player number with no controllers assigned, or null when all slots are taken. */
    fun nextFreePlayer(): PlayerNumber? =
        PlayerNumber.entries.firstOrNull { player -> _assignments.value.none { it.value == player } }

    fun addressesForPlayer(player: PlayerNumber): List<String> =
        _assignments.value.filterValues { it == player }.keys.toList()

    private fun isSlotTaken(side: Side, player: PlayerNumber): Boolean {
        val assignedToPlayer = _assignments.value.filterValues { it == player }.keys
        if (assignedToPlayer.any { sides[it] == Side.PRO }) return true
        return when (side) {
            Side.LEFT -> assignedToPlayer.any { sides[it] == Side.LEFT }
            Side.RIGHT -> assignedToPlayer.any { sides[it] == Side.RIGHT }
            Side.UNKNOWN -> assignedToPlayer.count { sides[it] == Side.UNKNOWN } >= 2
            Side.PRO -> assignedToPlayer.isNotEmpty()
        }
    }
}
