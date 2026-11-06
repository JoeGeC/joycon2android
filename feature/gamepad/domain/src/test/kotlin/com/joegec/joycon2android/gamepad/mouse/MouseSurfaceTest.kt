package com.joegec.joycon2android.gamepad.mouse

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.MouseSensor
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MouseSurfaceTest {

    private val onDesk = JoyconInput(accelX = 4096, mouse = MouseSensor(0, 0, 150))
    private val held = JoyconInput(accelX = 0, mouse = MouseSensor(0, 0, 150))

    private fun right(input: JoyconInput) = ConnectedJoycon("right", Side.RIGHT, "R", input = input)

    private fun left(input: JoyconInput) = ConnectedJoycon("left", Side.LEFT, "L", input = input)

    @Test
    fun `a Joy-Con in the hands leaves its buttons to the game`() {
        assertFalse(PlayerState(PlayerNumber.P1, right = right(held)).spendsButtonsOnMouse)
    }

    @Test
    fun `a Joy-Con on a surface spends its buttons on the mouse`() {
        assertTrue(PlayerState(PlayerNumber.P1, right = right(onDesk)).spendsButtonsOnMouse)
    }

    @Test
    fun `one of a pair on a surface spends both hands' buttons`() {
        val pair = PlayerState(PlayerNumber.P1, left = left(held), right = right(onDesk))

        assertTrue(pair.spendsButtonsOnMouse)
    }

    @Test
    fun `a controller with no sensor always spends them, as it can never stand on one`() {
        val pro = ConnectedJoycon("pro", Side.PRO, "Pro", input = JoyconInput())

        assertTrue(PlayerState(PlayerNumber.P1, left = pro).spendsButtonsOnMouse)
    }
}
