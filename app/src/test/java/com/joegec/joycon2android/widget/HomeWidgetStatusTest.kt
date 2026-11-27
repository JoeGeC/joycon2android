package com.joegec.joycon2android.widget

import com.joegec.joycon2android.ble.ScanBlocker
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.model.PlayerNumber
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeWidgetStatusTest {

    private val playing = HomeWidgetState(players = listOf(WidgetPlayer(PlayerBody(PlayerNumber.P1, JoyconSide.DUAL), false)))

    @Test
    fun `nothing to say before a scan`() {
        assertEquals(HomeWidgetStatus.READY, HomeWidgetStatus.of(HomeWidgetState()))
    }

    @Test
    fun `a blocker outranks everything else`() {
        val state = playing.copy(scanning = true, needsShizuku = true, blocker = ScanBlocker.BLUETOOTH_OFF)

        assertEquals(HomeWidgetStatus.BLUETOOTH_OFF, HomeWidgetStatus.of(state))
    }

    @Test
    fun `missing Shizuku outranks scanning`() {
        assertEquals(HomeWidgetStatus.NEEDS_SHIZUKU, HomeWidgetStatus.of(playing.copy(scanning = true, needsShizuku = true)))
    }

    @Test
    fun `scanning asks for SYNC`() {
        assertEquals(HomeWidgetStatus.SCANNING, HomeWidgetStatus.of(playing.copy(scanning = true)))
    }

    @Test
    fun `scanning with a controller waiting also asks for the combo`() {
        assertEquals(HomeWidgetStatus.SCANNING_WITH_WAITING, HomeWidgetStatus.of(playing.copy(scanning = true, waiting = 1)))
    }

    @Test
    fun `a connected controller without a player is waiting`() {
        assertEquals(HomeWidgetStatus.WAITING, HomeWidgetStatus.of(HomeWidgetState(waiting = 1)))
    }

    @Test
    fun `players with nothing missing are ready`() {
        assertEquals(HomeWidgetStatus.READY, HomeWidgetStatus.of(playing))
    }
}
