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
import org.junit.Assert.assertTrue
import org.junit.Test

class MouseRouterTest {

    private val enabled = MutableStateFlow(true)
    private val pushed = mutableListOf<MouseFrame>()
    private val off = mutableSetOf<PlayerNumber>()
    private val router = MouseRouter(enabled, { it in off }) { pushed += it }

    private fun joycon(address: String, side: Side, x: Int, distance: Int, vararg pressed: JoyconButton) =
        ConnectedJoycon(
            address,
            side,
            address,
            input = JoyconInput(
                pressed = pressed.map { it.id }.toSet(),
                accelX = if (side == Side.LEFT) -ONE_G else ONE_G, // standing on its rail
                mouse = MouseSensor(x, 0, distance),
            ),
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
    fun `a player's joy-con on a surface moves the pointer and clicks only through the mapping`() {
        router.route(pair(rightX = 100))
        router.route(pair(rightX = 130))

        assertEquals(MouseFrame(dx = 30), pushed.last())
    }

    @Test
    fun `a joy-con no player holds clicks with the defaults`() {
        val alone = { x: Int -> AppUiState(unassignedJoycons = listOf(joycon("right", Side.RIGHT, x, 150, JoyconButton.R))) }
        router.route(alone(100))
        router.route(alone(130))

        assertEquals(MouseFrame(dx = 30, buttons = setOf(MouseButton.PRIMARY)), pushed.last())
    }

    @Test
    fun `with every joy-con lifted the pointer is released`() {
        router.route(pair(rightX = 100, rightDistance = 3100))

        assertEquals(MouseFrame(), pushed.single())
    }

    @Test
    fun `two joy-cons on a surface both drive the pointer`() {
        router.route(pair(rightX = 0, leftDistance = 150))

        router.route(pair(rightX = 5, leftDistance = 150))

        assertEquals(5, pushed.last().dx)
    }

    @Test
    fun `turned off it pushes nothing`() {
        enabled.value = false

        router.route(pair(rightX = 100))

        assertTrue(pushed.isEmpty())
    }

    @Test
    fun `a player who switched their mouse off moves no pointer`() {
        off += PlayerNumber.P1
        router.route(pair(rightX = 100))
        router.route(pair(rightX = 130))

        assertEquals(MouseFrame(), pushed.last())
    }

    private companion object {
        const val ONE_G = 4096
    }
}
