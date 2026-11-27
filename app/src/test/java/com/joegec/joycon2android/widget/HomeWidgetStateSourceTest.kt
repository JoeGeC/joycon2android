package com.joegec.joycon2android.widget

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconConnectionState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import com.joegec.joycon2android.output.EmulatorKind
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.output.PlayingSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeWidgetStateSourceTest {

    private val left = ConnectedJoycon("L", Side.LEFT, "Joy-Con 2 (L)")
    private val right = ConnectedJoycon("R", Side.RIGHT, "Joy-Con 2 (R)")
    private val requested = mutableListOf<List<PlayerBody>>()

    private val eden = EmulatorTarget("eden", "dev.eden.eden_emulator", "Eden", EmulatorKind.EDEN)
    private val consoles = mutableListOf<Console>()

    private fun stateOf(
        session: AppUiState,
        playing: Playing = Playing.SWITCH,
        shizuku: Boolean = true,
    ) = runBlocking {
        HomeWidgetStateSource(
            session = flowOf(session),
            playing = flowOf(PlayingSelection(playing = playing)),
            emulatorFor = { if (it.playing == Playing.SWITCH) eden else null },
            shizukuAvailable = flowOf(shizuku),
            blocker = flowOf(null),
            observeGlobalMapping = ::mapping,
        ).observe().first()
    }

    private fun mapping(console: Console, bodies: List<PlayerBody>): Flow<GlobalMapping> {
        requested += bodies
        consoles += console
        return flowOf(GlobalMapping(emptyList(), emptyList()))
    }

    private fun session(vararg players: PlayerState, unassigned: List<ConnectedJoycon> = emptyList()) =
        AppUiState(players = players.toList(), unassignedJoycons = unassigned)

    @Test
    fun `a pair is one player holding both halves`() {
        val state = stateOf(session(PlayerState(PlayerNumber.P1, left, right)))

        assertEquals(PlayerBody(PlayerNumber.P1, JoyconSide.DUAL), state.players.single().body)
    }

    @Test
    fun `each controller's shell colour comes along, left then right`() {
        val red = left.copy(connectionState = JoyconConnectionState(accentColor = 0xFF3C28))
        val state = stateOf(session(PlayerState(PlayerNumber.P1, red, right)))

        assertEquals(listOf(0xFF3C28, null), state.players.single().shellColors)
    }

    @Test
    fun `the layout is looked up for the bodies being held`() {
        stateOf(session(PlayerState(PlayerNumber.P2, right = right)))

        assertEquals(listOf(listOf(PlayerBody(PlayerNumber.P2, JoyconSide.RIGHT))), requested)
    }

    @Test
    fun `nobody playing has no layout`() {
        val state = stateOf(session())

        assertNull(state.layout)
        assertTrue(requested.isEmpty())
    }

    @Test
    fun `a dropped half marks its player lost`() {
        val lost = left.copy(connectionState = JoyconConnectionState(connectionLost = true))

        assertTrue(stateOf(session(PlayerState(PlayerNumber.P1, lost, right))).players.single().connectionLost)
    }

    @Test
    fun `unassigned controllers are counted as waiting`() {
        assertEquals(2, stateOf(session(unassigned = listOf(left, right))).waiting)
    }

    @Test
    fun `missing Shizuku is only raised once someone plays a game that needs the pad`() {
        val player = PlayerState(PlayerNumber.P1, left)

        assertFalse(stateOf(session(), Playing.GAMECUBE, shizuku = false).needsShizuku)
        assertFalse(stateOf(session(player), Playing.SWITCH, shizuku = false).needsShizuku)
        assertTrue(stateOf(session(player), Playing.GAMECUBE, shizuku = false).needsShizuku)
    }

    @Test
    fun `the chosen emulator names the game`() {
        assertEquals("Eden", stateOf(session(), Playing.SWITCH).emulatorName)
        assertNull(stateOf(session(), Playing.PC).emulatorName)
    }

    @Test
    fun `the layout is the one for the console being played`() {
        stateOf(session(PlayerState(PlayerNumber.P1, left)), Playing.GAMECUBE)

        assertEquals(listOf(Console.GAMECUBE), consoles)
    }
}
