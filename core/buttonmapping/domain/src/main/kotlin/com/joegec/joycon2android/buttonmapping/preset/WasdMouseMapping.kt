package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.targetKey
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.KeyboardKey
import com.joegec.joycon2android.model.MouseAction

/** A PC game's walk, jump, use and hotbar keys, with a right Joy-Con on the desk as the mouse. */
object WasdMouseMapping : MappingPreset {
    override val id = "WASD_MOUSE"
    override val console = Console.PC

    override fun entries(side: JoyconSide): Map<String, String> =
        (keys(side).mapKeys { it.key.targetKey } + mouse(side).mapKeys { it.key.targetKey })
            .mapValues { (_, source) -> source.id }

    private fun keys(side: JoyconSide): Map<KeyboardKey, MappingSource> = when (side) {
        JoyconSide.DUAL, JoyconSide.GAMECUBE -> walk(StickSource.LEFT_STICK) + mapOf(
            KeyboardKey.SPACE to button(JoyconButton.B),
            KeyboardKey.E to button(JoyconButton.A),
            KeyboardKey.R to button(JoyconButton.X),
            KeyboardKey.F to button(JoyconButton.Y),
            KeyboardKey.Q to button(JoyconButton.L),
            KeyboardKey.TAB to button(JoyconButton.R),
            KeyboardKey.LEFT_SHIFT to button(JoyconButton.LS),
            KeyboardKey.LEFT_CTRL to button(JoyconButton.RS),
            KeyboardKey.ESCAPE to button(JoyconButton.Minus),
            KeyboardKey.ENTER to button(JoyconButton.Plus),
            KeyboardKey.DIGIT_1 to button(JoyconButton.Up),
            KeyboardKey.DIGIT_2 to button(JoyconButton.Right),
            KeyboardKey.DIGIT_3 to button(JoyconButton.Down),
            KeyboardKey.DIGIT_4 to button(JoyconButton.Left),
        )
        JoyconSide.LEFT -> walk(StickSource.LEFT_STICK) + mapOf(
            KeyboardKey.SPACE to button(JoyconButton.L),
            KeyboardKey.E to button(JoyconButton.ZL),
            KeyboardKey.LEFT_SHIFT to button(JoyconButton.LS),
            KeyboardKey.ESCAPE to button(JoyconButton.Minus),
            KeyboardKey.DIGIT_1 to button(JoyconButton.Up),
            KeyboardKey.DIGIT_2 to button(JoyconButton.Right),
            KeyboardKey.DIGIT_3 to button(JoyconButton.Down),
            KeyboardKey.DIGIT_4 to button(JoyconButton.Left),
        )
        JoyconSide.RIGHT -> mapOf(
            KeyboardKey.SPACE to button(JoyconButton.B),
            KeyboardKey.E to button(JoyconButton.A),
            KeyboardKey.R to button(JoyconButton.X),
            KeyboardKey.F to button(JoyconButton.Y),
            KeyboardKey.ESCAPE to button(JoyconButton.Plus),
        )
    }

    private fun mouse(side: JoyconSide): Map<MouseAction, MappingSource> = when (side) {
        JoyconSide.DUAL, JoyconSide.GAMECUBE -> mapOf(
            MouseAction.LEFT_CLICK to button(JoyconButton.ZR),
            MouseAction.RIGHT_CLICK to button(JoyconButton.ZL),
            MouseAction.SCROLL_UP to MappingSource.Stick(StickSource.RIGHT_STICK, StickDirection.UP),
            MouseAction.SCROLL_DOWN to MappingSource.Stick(StickSource.RIGHT_STICK, StickDirection.DOWN),
        )
        JoyconSide.LEFT -> emptyMap()
        JoyconSide.RIGHT -> mapOf(
            MouseAction.LEFT_CLICK to button(JoyconButton.ZR),
            MouseAction.RIGHT_CLICK to button(JoyconButton.R),
            MouseAction.SCROLL_UP to MappingSource.Stick(StickSource.RIGHT_STICK, StickDirection.UP),
            MouseAction.SCROLL_DOWN to MappingSource.Stick(StickSource.RIGHT_STICK, StickDirection.DOWN),
        )
    }

    private fun walk(stick: StickSource): Map<KeyboardKey, MappingSource> = mapOf(
        KeyboardKey.W to MappingSource.Stick(stick, StickDirection.UP),
        KeyboardKey.A to MappingSource.Stick(stick, StickDirection.LEFT),
        KeyboardKey.S to MappingSource.Stick(stick, StickDirection.DOWN),
        KeyboardKey.D to MappingSource.Stick(stick, StickDirection.RIGHT),
    )

    private fun button(button: JoyconButton) = MappingSource.Button(button)
}
