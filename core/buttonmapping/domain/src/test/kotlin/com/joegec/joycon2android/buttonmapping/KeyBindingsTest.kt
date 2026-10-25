package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.KeyboardKey
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class KeyBindingsTest {

    private val homeAndBack = KeyBindings(
        mapOf(
            AndroidKey.HOME to listOf(MappingSource.Button(JoyconButton.Home)),
            AndroidKey.BACK to listOf(MappingSource.Button(JoyconButton.Chat), MappingSource.Button(JoyconButton.Minus)),
        ),
    )
    private val wasd = KeyBindings(
        mapOf(
            KeyboardKey.W to listOf(MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.UP)),
            KeyboardKey.D to listOf(MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.RIGHT)),
        ),
    )

    private fun pair(vararg pressed: JoyconButton, stickX: Int = 2048, stickY: Int = 2048): PlayerState {
        val ids = pressed.map { it.id }.toSet()
        return PlayerState(
            PlayerNumber.P1,
            left = ConnectedJoycon("l", Side.LEFT, "L", input = JoyconInput(pressed = ids, stickX = stickX, stickY = stickY)),
            right = ConnectedJoycon("r", Side.RIGHT, "R", input = JoyconInput(pressed = ids)),
        )
    }

    private fun pro(rightStickX: Int, rightStickY: Int) = PlayerState(
        PlayerNumber.P1,
        left = ConnectedJoycon("p", Side.PRO, "Pro", input = JoyconInput(rightStickX = rightStickX, rightStickY = rightStickY)),
    )

    @Test
    fun `any bound button holds its key`() {
        assertEquals(setOf(AndroidKey.BACK), homeAndBack.heldKeys(pair(JoyconButton.Minus, JoyconButton.A)))
    }

    @Test
    fun `bound buttons are withheld from both Joy-Cons and the rest pass through`() {
        val withheld = homeAndBack.withheldFrom(pair(JoyconButton.Home, JoyconButton.Chat, JoyconButton.A))

        assertEquals(setOf(JoyconButton.A.id), withheld.left!!.input.pressed)
        assertEquals(setOf(JoyconButton.A.id), withheld.right!!.input.pressed)
    }

    @Test
    fun `nothing bound leaves the state untouched`() {
        val state = pair(JoyconButton.Home)

        assertEquals(emptySet<AndroidKey>(), KeyBindings<AndroidKey>().heldKeys(state))
        assertSame(state, KeyBindings<AndroidKey>().withheldFrom(state))
    }

    @Test
    fun `a stick holds its key past half tilt and not before`() {
        assertEquals(emptySet<KeyboardKey>(), wasd.heldKeys(pair(stickY = 2048 + 900)))
        assertEquals(setOf(KeyboardKey.W), wasd.heldKeys(pair(stickY = 2048 + 1100)))
    }

    @Test
    fun `a held stick key stays held a little below the tilt that pressed it`() {
        val easedBack = pair(stickY = 2048 + 800)

        assertEquals(setOf(KeyboardKey.W), wasd.heldKeys(easedBack, wasHeld = setOf(KeyboardKey.W)))
        assertEquals(emptySet<KeyboardKey>(), wasd.heldKeys(pair(stickY = 2048 + 600), wasHeld = setOf(KeyboardKey.W)))
    }

    @Test
    fun `a full diagonal holds both of its keys`() {
        assertEquals(setOf(KeyboardKey.W, KeyboardKey.D), wasd.heldKeys(pair(stickX = 2048 + 1450, stickY = 2048 + 1450)))
    }

    @Test
    fun `a bound direction is withheld and its opposite reaches the game`() {
        val up = wasd.withheldFrom(pair(stickX = 2048 - 1000, stickY = 2048 + 1500))
        val down = wasd.withheldFrom(pair(stickY = 2048 - 1500))

        assertEquals(2048, up.left!!.input.stickY)
        assertEquals(2048 - 1000, up.left!!.input.stickX)
        assertEquals(2048 - 1500, down.left!!.input.stickY)
    }

    @Test
    fun `a Pro Controller's right stick is read and withheld from its own axes`() {
        val arrows = KeyBindings(mapOf(KeyboardKey.UP to listOf(MappingSource.Stick(StickSource.RIGHT_STICK, StickDirection.UP))))
        val state = pro(rightStickX = 2048, rightStickY = 2048 + 1500)

        assertEquals(setOf(KeyboardKey.UP), arrows.heldKeys(state))
        assertEquals(2048, arrows.withheldFrom(state).left!!.input.rightStickY)
    }
}
