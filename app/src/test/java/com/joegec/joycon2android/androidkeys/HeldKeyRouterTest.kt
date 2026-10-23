package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.buttonmapping.KeyBindings
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.gamepad.GamepadFrame
import com.joegec.joycon2android.gamepad.keyboard.KeyboardFrame
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.KeyboardKey
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class HeldKeyRouterTest {

    private val gamepadOn = MutableStateFlow(true)
    private val dsuOn = MutableStateFlow(true)
    private val gamepadBindings = MutableStateFlow(mapOf(PlayerNumber.P1 to bindings(KeyboardKey.ENTER to JoyconButton.A)))
    private val dsuBindings = MutableStateFlow(mapOf(PlayerNumber.P1 to bindings(KeyboardKey.ESCAPE to JoyconButton.B)))

    private val router = HeldKeyRouter(
        gamepad = OutputBindings(gamepadOn, gamepadBindings),
        dsu = OutputBindings(dsuOn, dsuBindings),
    ) { held -> copy(keyboard = keyboard.copy(keys = held)) }

    private fun bindings(vararg keys: Pair<KeyboardKey, JoyconButton>) =
        KeyBindings(keys.associate { (key, button) -> key to listOf(MappingSource.Button(button)) })

    private fun player(input: JoyconInput) =
        PlayerState(PlayerNumber.P1, left = ConnectedJoycon("pro", Side.PRO, "Pro", input = input))

    private fun holding(vararg buttons: JoyconButton) = player(JoyconInput(pressed = buttons.map { it.id }.toSet()))

    /** [routed] is what reaches this router after earlier ones withheld their own buttons. */
    private fun route(player: PlayerState, routed: PlayerState = player) = router.route(
        AppUiState(players = listOf(player)),
        RoutedOutputs(
            gamepad = listOf(GamepadFrame(routed)),
            dsu = listOf(routed),
            keyboard = KeyboardFrame(androidKeys = setOf(AndroidKey.SCREENSHOT)),
        ),
    )

    @Test
    fun `each output holds its own console's keys and the Android keys stay`() {
        val keyboard = route(holding(JoyconButton.A, JoyconButton.B)).keyboard

        assertEquals(setOf(KeyboardKey.ENTER, KeyboardKey.ESCAPE), keyboard.keys)
        assertEquals(setOf(AndroidKey.SCREENSHOT), keyboard.androidKeys)
    }

    @Test
    fun `a bound button is withheld only from the output whose mapping binds it`() {
        val routed = route(holding(JoyconButton.A, JoyconButton.B))

        assertEquals(setOf(JoyconButton.B.id), routed.gamepad.single().state.left!!.input.pressed)
        assertEquals(setOf(JoyconButton.A.id), routed.dsu.single().left!!.input.pressed)
    }

    @Test
    fun `a button an earlier router withheld still fires its key here`() {
        val keyboard = route(holding(JoyconButton.A), routed = holding()).keyboard

        assertEquals(setOf(KeyboardKey.ENTER), keyboard.keys)
    }

    @Test
    fun `an output that is off holds nothing`() {
        dsuOn.value = false

        assertEquals(setOf(KeyboardKey.ENTER), route(holding(JoyconButton.A, JoyconButton.B)).keyboard.keys)
    }

    @Test
    fun `a stick key eased back below its press tilt stays held until it falls further`() {
        gamepadBindings.value = mapOf(
            PlayerNumber.P1 to KeyBindings(mapOf(KeyboardKey.W to listOf(MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.UP)))),
        )

        val tilts = listOf(2048 + 1200, 2048 + 800, 2048 + 500)
        val held = tilts.map { route(player(JoyconInput(stickY = it))).keyboard.keys }

        assertEquals(listOf(setOf(KeyboardKey.W), setOf(KeyboardKey.W), emptySet()), held)
    }
}
