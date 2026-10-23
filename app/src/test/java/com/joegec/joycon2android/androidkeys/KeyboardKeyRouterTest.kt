package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.gamepad.GamepadFrame
import com.joegec.joycon2android.gamepad.keyboard.KeyboardFrame
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.ButtonBindings
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

class KeyboardKeyRouterTest {

    private val gamepadOn = MutableStateFlow(true)
    private val dsuOn = MutableStateFlow(true)
    private val gamepadBindings = MutableStateFlow(mapOf(PlayerNumber.P1 to bindings(KeyboardKey.ENTER to JoyconButton.A)))
    private val dsuBindings = MutableStateFlow(mapOf(PlayerNumber.P1 to bindings(KeyboardKey.ESCAPE to JoyconButton.B)))

    private val router = KeyboardKeyRouter(
        gamepad = OutputBindings(gamepadOn, gamepadBindings),
        dsu = OutputBindings(dsuOn, dsuBindings),
    )

    private fun bindings(vararg keys: Pair<KeyboardKey, JoyconButton>) =
        ButtonBindings(keys.associate { (key, button) -> key to setOf(button) })

    private fun holding(vararg buttons: JoyconButton): RoutedOutputs {
        val pro = ConnectedJoycon("pro", Side.PRO, "Pro", input = JoyconInput(pressed = buttons.map { it.id }.toSet()))
        val player = PlayerState(PlayerNumber.P1, left = pro)
        return RoutedOutputs(
            gamepad = listOf(GamepadFrame(player)),
            dsu = listOf(player),
            keyboard = KeyboardFrame(androidKeys = setOf(AndroidKey.SCREENSHOT)),
        )
    }

    @Test
    fun `each output holds its own console's keys and the Android keys stay`() {
        val keyboard = router.route(holding(JoyconButton.A, JoyconButton.B)).keyboard

        assertEquals(setOf(KeyboardKey.ENTER, KeyboardKey.ESCAPE), keyboard.keys)
        assertEquals(setOf(AndroidKey.SCREENSHOT), keyboard.androidKeys)
    }

    @Test
    fun `a bound button is withheld only from the output whose mapping binds it`() {
        val routed = router.route(holding(JoyconButton.A, JoyconButton.B))

        assertEquals(setOf(JoyconButton.B.id), routed.gamepad.single().state.left!!.input.pressed)
        assertEquals(setOf(JoyconButton.A.id), routed.dsu.single().left!!.input.pressed)
    }

    @Test
    fun `an output that is off holds nothing`() {
        dsuOn.value = false

        assertEquals(setOf(KeyboardKey.ENTER), router.route(holding(JoyconButton.A, JoyconButton.B)).keyboard.keys)
    }
}
