package com.joegec.joycon2android.mouse

import com.joegec.joycon2android.gamepad.mouse.MouseButton
import com.joegec.joycon2android.gamepad.mouse.MouseFrame
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.MouseSensor
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MouseRouterTest {

    private val enabled = MutableStateFlow(true)
    private val pushed = mutableListOf<MouseFrame>()
    private val router = MouseRouter(enabled) { pushed += it }

    private fun joycon(address: String, side: Side, x: Int, distance: Int, vararg pressed: JoyconButton) =
        ConnectedJoycon(
            address,
            side,
            address,
            input = JoyconInput(pressed = pressed.map { it.id }.toSet(), mouse = MouseSensor(x, 0, distance)),
        )

    private fun pair(rightX: Int, rightDistance: Int = 150, leftDistance: Int = 3100) = AppUiState(
        players = listOf(
            PlayerState(
                PlayerNumber.P1,
                left = joycon("left", Side.LEFT, 0, leftDistance, JoyconButton.L),
                right = joycon("right", Side.RIGHT, rightX, rightDistance, JoyconButton.R),
            ),
        ),
    )

    @Test
    fun `a joy-con on a surface moves the pointer and keeps its clicks from the game`() {
        router.route(pair(rightX = 100))
        val routed = router.route(pair(rightX = 130))

        assertEquals(MouseFrame(dx = 30, buttons = setOf(MouseButton.PRIMARY)), pushed.last())
        val player = routed.players.single()
        assertTrue(player.right!!.input.pressed.isEmpty())
        assertEquals(setOf(JoyconButton.L.id), player.left!!.input.pressed)
    }

    @Test
    fun `with every joy-con lifted the game gets everything and the pointer is released`() {
        val state = pair(rightX = 100, rightDistance = 3100)

        assertSame(state, router.route(state))
        assertEquals(MouseFrame(), pushed.single())
    }

    @Test
    fun `two joy-cons on a surface both drive the pointer`() {
        router.route(pair(rightX = 0, leftDistance = 150))

        router.route(pair(rightX = 5, leftDistance = 150))

        assertEquals(setOf(MouseButton.PRIMARY), pushed.last().buttons)
        assertEquals(5, pushed.last().dx)
    }

    @Test
    fun `turned off it touches nothing`() {
        enabled.value = false
        val state = pair(rightX = 100)

        assertSame(state, router.route(state))
        assertTrue(pushed.isEmpty())
    }
}
