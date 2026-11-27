package com.joegec.joycon2android.widget

import com.joegec.joycon2android.ble.ScanBlocker
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.body
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.PlayingSelection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class HomeWidgetStateSource(
    private val session: Flow<AppUiState>,
    private val playing: Flow<PlayingSelection>,
    private val emulatorFor: (PlayingSelection) -> EmulatorTarget?,
    private val shizukuAvailable: Flow<Boolean>,
    private val blocker: Flow<ScanBlocker?>,
    private val observeGlobalMapping: (Console, List<PlayerBody>) -> Flow<GlobalMapping>,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<HomeWidgetState> {
        val roster = session.map(::roster).distinctUntilChanged()
        return combine(roster, playing, shizukuAvailable, ::Triple)
            .flatMapLatest { (roster, selection, shizuku) ->
                val withGame = roster.copy(
                    playing = selection.playing,
                    emulatorName = emulatorFor(selection)?.name,
                    needsShizuku = !shizuku && !selection.playing.buttonsOverDsu && roster.players.isNotEmpty(),
                )
                layoutFor(selection.playing.console, roster.players.map { it.body }).map { withGame.copy(layout = it) }
            }
            .combine(blocker) { state, blocker -> state.copy(blocker = blocker) }
            .distinctUntilChanged()
    }

    private fun layoutFor(console: Console, bodies: List<PlayerBody>): Flow<GlobalMapping?> =
        if (bodies.isEmpty()) flowOf(null) else observeGlobalMapping(console, bodies)

    private fun roster(session: AppUiState) = HomeWidgetState(
        scanning = session.scanning,
        players = session.activePlayers.mapNotNull { player ->
            player.body()?.let { body ->
                val held = listOfNotNull(player.left, player.right)
                WidgetPlayer(body, held.any { it.connectionLost }, held.map { it.accentColor })
            }
        },
        waiting = session.unassignedJoycons.size,
    )
}
