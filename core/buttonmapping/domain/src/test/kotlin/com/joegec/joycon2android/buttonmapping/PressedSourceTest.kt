package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PressedSourceTest {

    private fun pair(pressed: Set<String> = emptySet(), leftStickY: Int = 2048, rightStickX: Int = 2048) = PlayerState(
        PlayerNumber.P1,
        left = ConnectedJoycon("l", Side.LEFT, "L", input = JoyconInput(pressed = pressed, stickY = leftStickY)),
        right = ConnectedJoycon("r", Side.RIGHT, "R", input = JoyconInput(stickX = rightStickX)),
    )

    private fun loneLeft(stickY: Int) = PlayerState(
        PlayerNumber.P1,
        left = ConnectedJoycon("l", Side.LEFT, "L", input = JoyconInput(stickY = stickY)),
    )

    private fun PlayerState.since(previous: PlayerState, side: JoyconSide = JoyconSide.DUAL, heldSideways: Boolean = false) =
        sourcePressedSince(previous, side, heldSideways)

    @Test
    fun `a button pressed this frame is the source`() {
        assertEquals(MappingSource.Button(JoyconButton.ZL), pair(setOf("ZL")).since(pair()))
    }

    @Test
    fun `a button already held is not pressed again`() {
        assertNull(pair(setOf("ZL")).since(pair(setOf("ZL"))))
    }

    @Test
    fun `a stick counts once it leans past half-way`() {
        assertNull(pair(leftStickY = 2048 + 900).since(pair()))
        assertEquals(
            MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.UP),
            pair(leftStickY = 2048 + 1200).since(pair()),
        )
    }

    @Test
    fun `the stick leaned furthest wins`() {
        val now = pair(leftStickY = 2048 + 1100, rightStickX = 2048 - 1900)

        assertEquals(MappingSource.Stick(StickSource.RIGHT_STICK, StickDirection.LEFT), now.since(pair()))
    }

    @Test
    fun `a stick held from before is not leaned again`() {
        assertNull(pair(leftStickY = 4000).since(pair(leftStickY = 4000)))
    }

    @Test
    fun `a sideways Joy-Con's stick comes back in the frame its mapping is stored in`() {
        val leaned = loneLeft(stickY = 4000).since(loneLeft(stickY = 2048), JoyconSide.LEFT, heldSideways = true)

        assertEquals(MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.LEFT), leaned)
    }

    @Test
    fun `an upright Joy-Con's stick stays in the hardware's frame`() {
        val leaned = loneLeft(stickY = 4000).since(loneLeft(stickY = 2048), JoyconSide.LEFT, heldSideways = false)

        assertEquals(MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.UP), leaned)
    }
}
