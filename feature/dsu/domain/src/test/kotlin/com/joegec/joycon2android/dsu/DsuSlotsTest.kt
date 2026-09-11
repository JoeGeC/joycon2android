package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Test

class DsuSlotsTest {

    private fun joycon(side: Side) = ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con")

    private fun pair(player: PlayerNumber) =
        PlayerState(player, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

    private fun solo(player: PlayerNumber) = PlayerState(player, right = joycon(Side.RIGHT))

    @Test
    fun `a player streams on the slot its number gives it`() {
        val streams = DsuSlots.streams(listOf(solo(PlayerNumber.P1), solo(PlayerNumber.P3)))

        assertEquals(listOf(0, 2), streams.map { it.slot })
    }

    @Test
    fun `a pair's second hand takes the highest free slot and reports only that joycon`() {
        val streams = DsuSlots.streams(listOf(pair(PlayerNumber.P1)))

        assertEquals(listOf(0, 3), streams.map { it.slot })
        val secondHand = streams.last().state
        assertEquals(Side.LEFT, secondHand.motionSource?.side)
        assertEquals(null, secondHand.right)
    }

    @Test
    fun `two pairs fill downwards in player order`() {
        val streams = DsuSlots.secondHands(listOf(pair(PlayerNumber.P1), pair(PlayerNumber.P2)))

        assertEquals(listOf(PlayerNumber.P1 to 3, PlayerNumber.P2 to 2), streams.map { it.state.player to it.slot })
    }

    @Test
    fun `players win the slots their numbers give them, so a crowded session drops second hands`() {
        val players = listOf(pair(PlayerNumber.P1), solo(PlayerNumber.P2), solo(PlayerNumber.P3), solo(PlayerNumber.P4))

        assertEquals(emptyList<DsuStream>(), DsuSlots.secondHands(players))
        assertEquals(listOf(0, 1, 2, 3), DsuSlots.streams(players).map { it.slot })
    }

    @Test
    fun `a solo joycon has no second hand`() {
        assertEquals(emptyList<DsuStream>(), DsuSlots.secondHands(listOf(solo(PlayerNumber.P1))))
    }

    @Test
    fun `players beyond the four slots are not served`() {
        val streams = DsuSlots.streams(listOf(solo(PlayerNumber.P5), solo(PlayerNumber.P1)))

        assertEquals(listOf(0), streams.map { it.slot })
    }
}
