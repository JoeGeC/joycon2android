package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AndroidKeyBindingsTest {

    private val homeAndBack = AndroidKeyBindings(
        mapOf(
            AndroidKey.HOME to setOf(JoyconButton.Home),
            AndroidKey.BACK to setOf(JoyconButton.Chat, JoyconButton.Minus),
        ),
    )

    private fun pair(vararg pressed: JoyconButton): PlayerState {
        val ids = pressed.map { it.id }.toSet()
        return PlayerState(
            PlayerNumber.P1,
            left = ConnectedJoycon("l", Side.LEFT, "L", input = JoyconInput(pressed = ids)),
            right = ConnectedJoycon("r", Side.RIGHT, "R", input = JoyconInput(pressed = ids)),
        )
    }

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

        assertEquals(emptySet<AndroidKey>(), AndroidKeyBindings().heldKeys(state))
        assertSame(state, AndroidKeyBindings().withheldFrom(state))
    }
}
