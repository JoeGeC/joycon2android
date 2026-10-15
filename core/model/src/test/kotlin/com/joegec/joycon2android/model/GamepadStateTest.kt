package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GamepadStateTest {

    private fun joycon(side: Side, input: JoyconInput) =
        ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con", input = input)

    private val tilted = JoyconInput(stickX = 3000, stickY = 1000)

    @Test
    fun `a single left Joy-Con keeps its own buttons and an unturned left stick`() {
        val input = tilted.copy(pressed = setOf(JoyconButton.Down.id, JoyconButton.L.id))
        val state = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT, input), asSingleJoycon = true).gamepad

        assertEquals(setOf(JoyconButton.Down.id, JoyconButton.L.id), state.pressed)
        assertEquals(3000 to 1000, state.leftStickX to state.leftStickY)
    }

    @Test
    fun `a single right Joy-Con keeps its stick on the right`() {
        val state = PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT, tilted), asSingleJoycon = true).gamepad

        assertEquals(3000 to 1000, state.rightStickX to state.rightStickY)
        assertEquals(2048 to 2048, state.leftStickX to state.leftStickY)
    }

    @Test
    fun `a single Joy-Con's SL and SR ride the shoulders its body lacks`() {
        val left = tilted.copy(pressed = setOf(JoyconButton.SlLeft.id, JoyconButton.SrLeft.id))
        val right = tilted.copy(pressed = setOf(JoyconButton.SlRight.id, JoyconButton.SrRight.id))

        val leftState = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT, left), asSingleJoycon = true).gamepad
        val rightState = PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT, right), asSingleJoycon = true).gamepad

        assertEquals(setOf(JoyconButton.R.id, JoyconButton.ZR.id), leftState.pressed)
        assertEquals(setOf(JoyconButton.L.id, JoyconButton.ZL.id), rightState.pressed)
    }

    @Test
    fun `a sideways left Joy-Con still turns its cluster onto the faces`() {
        val input = tilted.copy(pressed = setOf(JoyconButton.Down.id))
        val state = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT, input)).gamepad

        assertEquals(setOf(JoyconButton.A.id), state.pressed)
    }
}
