package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.directionKey
import com.joegec.joycon2android.buttonmapping.physicalButtons
import com.joegec.joycon2android.buttonmapping.preset.AndroidMapping
import com.joegec.joycon2android.buttonmapping.preset.AndroidXboxMapping
import com.joegec.joycon2android.buttonmapping.target.PadButton
import com.joegec.joycon2android.buttonmapping.target.PadStick
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.GamepadState
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidPadLayoutTest {

    private fun input(pressed: Set<JoyconButton>, stickX: Int = 2048, stickY: Int = 2048) =
        JoyconInput(pressed = pressed.map { it.id }.toSet(), stickX = stickX, stickY = stickY)

    private fun stateOf(side: JoyconSide, pressed: Set<JoyconButton> = emptySet(), stickX: Int = 2048, stickY: Int = 2048) =
        when (side) {
            JoyconSide.DUAL -> PlayerState(
                PlayerNumber.P1,
                left = ConnectedJoycon("l", Side.LEFT, "L", input = input(pressed, stickX, stickY)),
                right = ConnectedJoycon("r", Side.RIGHT, "R", input = input(pressed)),
            )
            JoyconSide.LEFT -> PlayerState(PlayerNumber.P1, left = ConnectedJoycon("l", Side.LEFT, "L", input = input(pressed, stickX, stickY)))
            JoyconSide.RIGHT -> PlayerState(PlayerNumber.P1, right = ConnectedJoycon("r", Side.RIGHT, "R", input = input(pressed, stickX, stickY)))
            JoyconSide.GAMECUBE -> ConnectedJoycon("g", Side.PRO, "GC", model = ControllerModel.GAMECUBE, input = input(pressed, stickX, stickY))
                .let { PlayerState(PlayerNumber.P1, left = it, right = it) }
        }

    private fun asLabelled(side: JoyconSide) = AndroidPadLayout.of(AndroidMapping.entries(side), side)

    // A pair's SL/SR have no code on the pad, so the game never sees them.
    private fun GamepadState.sent() = copy(pressed = pressed intersect PadButton.entries.map { it.slot.id }.toSet())

    @Test
    fun `as labelled sends each button exactly as the unmapped pad did`() {
        JoyconSide.entries.forEach { side ->
            side.physicalButtons().forEach { button ->
                val state = stateOf(side, setOf(button))

                assertEquals("$side $button", state.gamepad.sent(), asLabelled(side).padFor(state))
            }
        }
    }

    @Test
    fun `as labelled keeps each stick analog, a sideways Joy-Con's still turned`() {
        JoyconSide.entries.forEach { side ->
            val state = stateOf(side, stickX = 3000, stickY = 1200)

            assertEquals("$side", state.gamepad, asLabelled(side).padFor(state))
        }
    }

    @Test
    fun `by position sends A for the bottom button and B for the right one`() {
        val layout = AndroidPadLayout.of(AndroidXboxMapping.entries(JoyconSide.DUAL), JoyconSide.DUAL)

        assertEquals(setOf(JoyconButton.A.id), layout.padFor(stateOf(JoyconSide.DUAL, setOf(JoyconButton.B))).pressed)
        assertEquals(setOf(JoyconButton.B.id), layout.padFor(stateOf(JoyconSide.DUAL, setOf(JoyconButton.A))).pressed)
        assertEquals(setOf(JoyconButton.X.id), layout.padFor(stateOf(JoyconSide.DUAL, setOf(JoyconButton.Y))).pressed)
    }

    @Test
    fun `a button bound to ZR pulls the right trigger`() {
        val entries = AndroidMapping.entries(JoyconSide.DUAL) + (PadButton.ZR.name to JoyconButton.R.name)
        val pad = AndroidPadLayout.of(entries, JoyconSide.DUAL).padFor(stateOf(JoyconSide.DUAL, setOf(JoyconButton.R)))

        assertEquals(setOf(JoyconButton.ZR.id, JoyconButton.R.id), pad.pressed)
        assertEquals(255, pad.rightTrigger)
    }

    @Test
    fun `a stick driven by buttons is pushed to the gate`() {
        val entries = AndroidMapping.entries(JoyconSide.DUAL) +
            (PadStick.LStick.directionKey(StickDirection.UP) to JoyconButton.X.name)
        val pad = AndroidPadLayout.of(entries, JoyconSide.DUAL).padFor(stateOf(JoyconSide.DUAL, setOf(JoyconButton.X)))

        assertEquals(2048, pad.leftStickX)
        assertEquals(4096, pad.leftStickY)
    }

    @Test
    fun `the right stick can take the left stick whole`() {
        val entries = AndroidMapping.entries(JoyconSide.DUAL) + StickDirection.entries.associate {
            PadStick.RStick.directionKey(it) to MappingSource.Stick(StickSource.LEFT_STICK, it).id
        }
        val pad = AndroidPadLayout.of(entries, JoyconSide.DUAL).padFor(stateOf(JoyconSide.DUAL, stickX = 3000, stickY = 1200))

        assertEquals(3000 to 1200, pad.rightStickX to pad.rightStickY)
    }

    @Test
    fun `a target cleared to none sends nothing`() {
        val entries = AndroidMapping.entries(JoyconSide.DUAL) + (PadButton.A.name to "")

        assertEquals(emptySet<String>(), AndroidPadLayout.of(entries, JoyconSide.DUAL).padFor(stateOf(JoyconSide.DUAL, setOf(JoyconButton.A))).pressed)
    }
}
