package com.joegec.joycon2android.session

import com.joegec.joycon2android.assignment.AssignmentRepository
import com.joegec.joycon2android.assignment.ComboAssignmentDetector
import com.joegec.joycon2android.assignment.PlayerStateResolver
import com.joegec.joycon2android.connection.ControllerRepository
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionCoordinatorTypeTest {

    private val controllers = FakeControllers()
    private val assignments = FakeAssignments()
    private val unassignedPlayers = mutableListOf<PlayerNumber>()
    private val coordinator = SessionCoordinator(
        scope = CoroutineScope(Dispatchers.Unconfined),
        controllers = controllers,
        assignments = assignments,
        resolver = PlayerStateResolver(evictConflicting = {}),
        comboDetector = ComboAssignmentDetector(),
        onState = {},
        onPlayerAssigned = {},
        onPlayerUnassigned = { unassignedPlayers += it },
    )

    @Test
    fun `an assigned controller leaves its player before its type changes`() {
        assignments.players[ADDRESS] = PlayerNumber.P2

        coordinator.setControllerType(ADDRESS, Side.PRO)

        assertNull(assignments.players[ADDRESS])
        assertEquals(listOf(PlayerNumber.P2), unassignedPlayers)
        assertEquals(ADDRESS to Side.PRO, controllers.typeChanges.single())
    }

    @Test
    fun `an unassigned controller just changes type`() {
        coordinator.setControllerType(ADDRESS, null)

        assertEquals(emptyList<PlayerNumber>(), unassignedPlayers)
        assertEquals(ADDRESS to null, controllers.typeChanges.single())
    }

    private class FakeControllers : ControllerRepository {
        val typeChanges = mutableListOf<Pair<String, Side?>>()
        override val controllers: StateFlow<List<ConnectedJoycon>> = MutableStateFlow(emptyList())
        override val scanning: StateFlow<Boolean> = MutableStateFlow(false)
        override val error: StateFlow<String?> = MutableStateFlow(null)
        override fun startScan() = Unit
        override fun stopScan() = Unit
        override fun disconnect(address: String) = Unit
        override fun disconnectAll() = Unit
        override fun setPlayerLed(address: String, player: PlayerNumber?) = Unit
        override fun setControllerType(address: String, side: Side?) {
            typeChanges += address to side
        }
        override fun emitError(message: String) = Unit
    }

    private class FakeAssignments : AssignmentRepository {
        val players = mutableMapOf<String, PlayerNumber>()
        override val assignments: StateFlow<Map<String, PlayerNumber>> = MutableStateFlow(emptyMap())
        override fun assign(address: String, side: Side, player: PlayerNumber): Boolean = players.put(address, player) == null
        override fun unassign(address: String) {
            players.remove(address)
        }
        override fun unassignAll() = players.clear()
        override fun getPlayer(address: String): PlayerNumber? = players[address]
        override fun nextFreePlayer(): PlayerNumber? = null
        override fun addressesForPlayer(player: PlayerNumber): List<String> = players.filterValues { it == player }.keys.toList()
    }

    private companion object {
        const val ADDRESS = "3C:A9:AB:5E:6A:A6"
    }
}
