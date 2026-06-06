package com.joegec.joycon2android.domain

import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Maps Joy-Con BLE addresses to player numbers.
 * Pure domain logic with no Android dependencies.
 */
class PlayerAssignmentManager {

    private val _assignments = MutableStateFlow<Map<String, PlayerNumber>>(emptyMap())
    val assignments: StateFlow<Map<String, PlayerNumber>> = _assignments.asStateFlow()

    fun assign(address: String, player: PlayerNumber) {
        _assignments.value = _assignments.value + (address to player)
    }

    fun unassign(address: String) {
        _assignments.value = _assignments.value - address
    }

    fun unassignAll() {
        _assignments.value = emptyMap()
    }

    fun getPlayer(address: String): PlayerNumber? = _assignments.value[address]

    fun addressesForPlayer(player: PlayerNumber): List<String> =
        _assignments.value.filterValues { it == player }.keys.toList()
}
