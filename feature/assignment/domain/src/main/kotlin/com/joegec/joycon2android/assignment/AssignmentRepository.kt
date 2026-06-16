package com.joegec.joycon2android.assignment

import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds which Joy-Con (by BLE address) is assigned to which player, enforcing one Left +
 * one Right (or one Pro) per player. Implemented in the data layer.
 */
interface AssignmentRepository {
    val assignments: StateFlow<Map<String, PlayerNumber>>

    fun assign(address: String, side: Side, player: PlayerNumber): Boolean
    fun unassign(address: String)
    fun unassignAll()
    fun getPlayer(address: String): PlayerNumber?
    fun nextFreePlayer(): PlayerNumber?
    fun addressesForPlayer(player: PlayerNumber): List<String>
}
