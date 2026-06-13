package com.joegec.joycon2android.domain

import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlayerAssignmentManagerTest {

    private val manager = PlayerAssignmentManager()

    @Test
    fun `nextFreePlayer starts at P1`() {
        assertEquals(PlayerNumber.P1, manager.nextFreePlayer())
    }

    @Test
    fun `nextFreePlayer skips players with any controller assigned`() {
        manager.assign("AA", Side.LEFT, PlayerNumber.P1)

        assertEquals(PlayerNumber.P2, manager.nextFreePlayer())
    }

    @Test
    fun `nextFreePlayer fills gaps left by unassignment`() {
        manager.assign("AA", Side.PRO, PlayerNumber.P1)
        manager.assign("BB", Side.PRO, PlayerNumber.P2)
        manager.unassign("AA")

        assertEquals(PlayerNumber.P1, manager.nextFreePlayer())
    }

    @Test
    fun `nextFreePlayer is null when all slots are taken`() {
        PlayerNumber.entries.forEachIndexed { index, player ->
            manager.assign("ADDR$index", Side.PRO, player)
        }

        assertNull(manager.nextFreePlayer())
    }
}
