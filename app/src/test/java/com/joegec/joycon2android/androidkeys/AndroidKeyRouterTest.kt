package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.buttonmapping.KeyBindings
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidKeyRouterTest {

    private val running = MutableStateFlow(true)
    private val padOn = MutableStateFlow(true)
    private val keyboardOn = MutableStateFlow(false)
    private val bindings = MutableStateFlow(
        mapOf(PlayerNumber.P1 to bindings(AndroidKey.HOME to JoyconButton.Home, AndroidKey.BACK to JoyconButton.Minus)),
    )
    private val pressed = mutableListOf<AndroidKey>()

    private val router = AndroidKeyRouter(
        bindings = OutputBindings(running, bindings),
        padEnabled = padOn,
        keyboardEnabled = keyboardOn,
        pressKey = { pressed += it },
    )

    private fun bindings(vararg keys: Pair<AndroidKey, JoyconButton>) =
        KeyBindings(keys.associate { (key, button) -> key to listOf(MappingSource.Button(button)) })

    private fun holding(vararg buttons: JoyconButton): AppUiState {
        val pro = ConnectedJoycon("pro", Side.PRO, "Pro", input = JoyconInput(pressed = buttons.map { it.id }.toSet()))
        return AppUiState(players = listOf(PlayerState(PlayerNumber.P1, left = pro)))
    }

    @Test
    fun `the pad sends Home itself and withholds its button from the game`() {
        val frame = router.route(holding(JoyconButton.Home, JoyconButton.A)).gamepad.single()

        assertEquals(setOf(AndroidKey.HOME), frame.androidKeys)
        assertEquals(setOf(JoyconButton.A.id), frame.state.left!!.input.pressed)
        assertEquals(emptyList<AndroidKey>(), pressed)
    }

    @Test
    fun `screenshot goes through the shell once per press however long it is held`() {
        bindings.value = mapOf(PlayerNumber.P1 to bindings(AndroidKey.SCREENSHOT to JoyconButton.Capture))

        repeat(3) { router.route(holding(JoyconButton.Capture)) }
        router.route(holding())
        router.route(holding(JoyconButton.Capture))

        assertEquals(listOf(AndroidKey.SCREENSHOT, AndroidKey.SCREENSHOT), pressed)
    }

    @Test
    fun `without the pad every key goes through the shell and is withheld from DSU`() {
        padOn.value = false

        val routed = router.route(holding(JoyconButton.Minus, JoyconButton.A))

        assertEquals(listOf(AndroidKey.BACK), pressed)
        assertEquals(setOf(JoyconButton.A.id), routed.dsu.single().left!!.input.pressed)
    }

    @Test
    fun `with the output off nothing fires and nothing is withheld`() {
        running.value = false

        val routed = router.route(holding(JoyconButton.Home, JoyconButton.Minus))

        assertEquals(emptyList<AndroidKey>(), pressed)
        assertEquals(setOf(JoyconButton.Home.id, JoyconButton.Minus.id), routed.dsu.single().left!!.input.pressed)
    }

    @Test
    fun `with the keyboard up and no pad the keyboard holds every key instead of the shell`() {
        padOn.value = false
        keyboardOn.value = true

        val held = router.route(holding(JoyconButton.Minus)).keyboard
        val released = router.route(holding()).keyboard

        assertEquals(setOf(AndroidKey.BACK), held.androidKeys)
        assertEquals(emptySet<AndroidKey>(), released.androidKeys)
        assertEquals(emptyList<AndroidKey>(), pressed)
    }

    @Test
    fun `with the keyboard up the pad still sends Home and the keyboard takes screenshot`() {
        keyboardOn.value = true
        bindings.value = mapOf(
            PlayerNumber.P1 to bindings(AndroidKey.HOME to JoyconButton.Home, AndroidKey.SCREENSHOT to JoyconButton.Capture),
        )

        val routed = router.route(holding(JoyconButton.Home, JoyconButton.Capture))

        assertEquals(setOf(AndroidKey.HOME), routed.gamepad.single().androidKeys)
        assertEquals(setOf(AndroidKey.SCREENSHOT), routed.keyboard.androidKeys)
    }
}
