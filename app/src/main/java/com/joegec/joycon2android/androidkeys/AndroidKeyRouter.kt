package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.gamepad.GamepadFrame
import com.joegec.joycon2android.gamepad.keyboard.KeyboardFrame
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/** How each key reaches Android: docs/virtual-gamepad.md#android-keys */
class AndroidKeyRouter(
    private val bindings: OutputBindings<AndroidKey>,
    private val padEnabled: StateFlow<Boolean>,
    private val keyboardEnabled: StateFlow<Boolean>,
    private val pressKey: (AndroidKey) -> Unit,
) {
    private var heldThroughShell = emptyMap<PlayerNumber, Set<AndroidKey>>()

    fun route(state: AppUiState): RoutedOutputs {
        val notOnPad = state.players.associate { it.player to bindings.heldKeys(it) - sentByPad(it) }
        val onKeyboard = keyboardEnabled.value
        if (onKeyboard) heldThroughShell = notOnPad else pressNewlyHeld(notOnPad)
        return RoutedOutputs(
            gamepad = state.players.map { GamepadFrame(bindings.of(it).withheldFrom(it), sentByPad(it)) },
            dsu = state.activePlayers.map { bindings.of(it).withheldFrom(it) },
            keyboard = KeyboardFrame(androidKeys = if (onKeyboard) notOnPad.values.flatten().toSet() else emptySet()),
        )
    }

    private fun sentByPad(player: PlayerState): Set<AndroidKey> =
        if (padEnabled.value) bindings.heldKeys(player) intersect GamepadFrame.SENDABLE_KEYS else emptySet()

    // The shell presses once per press, so a key held across frames fires only as it goes down.
    private fun pressNewlyHeld(held: Map<PlayerNumber, Set<AndroidKey>>) {
        held.forEach { (player, keys) -> (keys - heldThroughShell[player].orEmpty()).forEach(pressKey) }
        heldThroughShell = held
    }
}
