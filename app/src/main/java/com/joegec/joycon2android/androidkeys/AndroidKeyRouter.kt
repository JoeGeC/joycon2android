package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.gamepad.GamepadFrame
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/** How each key reaches Android: docs/virtual-gamepad.md#android-keys */
class AndroidKeyRouter(
    private val gamepad: OutputAndroidKeys,
    private val dsu: OutputAndroidKeys,
    private val keyboardEnabled: StateFlow<Boolean>,
    private val pushKeyboard: (Set<AndroidKey>) -> Unit,
    private val pressKey: (AndroidKey) -> Unit,
) {
    private var heldThroughShell = emptyMap<PlayerNumber, Set<AndroidKey>>()

    fun route(state: AppUiState): RoutedOutputs {
        val routed = RoutedOutputs(
            gamepad = state.players.map { GamepadFrame(gamepad.of(it).withheldFrom(it), sentByPad(it)) },
            dsu = state.activePlayers.map { dsu.of(it).withheldFrom(it) },
        )
        val notOnPad = state.players.associate { it.player to held(it) - sentByPad(it) }
        if (keyboardEnabled.value) sendThroughKeyboard(notOnPad) else pressNewlyHeld(notOnPad)
        return routed
    }

    private fun held(player: PlayerState) = gamepad.heldKeys(player) + dsu.heldKeys(player)

    private fun sentByPad(player: PlayerState): Set<AndroidKey> =
        if (gamepad.isEnabled) held(player) intersect GamepadFrame.SENDABLE_KEYS else emptySet()

    private fun sendThroughKeyboard(held: Map<PlayerNumber, Set<AndroidKey>>) {
        pushKeyboard(held.values.flatten().toSet())
        heldThroughShell = held
    }

    // The shell presses once per press, so a key held across frames fires only as it goes down.
    private fun pressNewlyHeld(held: Map<PlayerNumber, Set<AndroidKey>>) {
        held.forEach { (player, keys) -> (keys - heldThroughShell[player].orEmpty()).forEach(pressKey) }
        heldThroughShell = held
    }
}
