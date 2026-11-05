package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.SwitchProMapping
import com.joegec.joycon2android.buttonmapping.preset.WasdMouseMapping
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.MouseAction
import org.junit.Assert.assertEquals
import org.junit.Test

class MouseDefaultsTest {

    private fun applied(side: JoyconSide) = MappingLayouts.entriesOf(Console.SWITCH_PRO, side, SwitchProMapping)

    @Test
    fun `a pair or Pro mouses with R, ZR, RS and the right stick`() {
        val entries = applied(JoyconSide.DUAL)

        assertEquals("R", entries[MouseAction.LEFT_CLICK.targetKey])
        assertEquals("ZR", entries[MouseAction.RIGHT_CLICK.targetKey])
        assertEquals("RS", entries[MouseAction.MIDDLE_CLICK.targetKey])
        assertEquals("RIGHT_STICK_UP", entries[MouseAction.SCROLL_UP.targetKey])
    }

    @Test
    fun `a lone Joy-Con clicks and scrolls with its own buttons and stick`() {
        val left = applied(JoyconSide.LEFT)
        val right = applied(JoyconSide.RIGHT)

        assertEquals("L", left[MouseAction.LEFT_CLICK.targetKey])
        assertEquals("LS", left[MouseAction.MIDDLE_CLICK.targetKey])
        assertEquals("LEFT_STICK_DOWN", left[MouseAction.SCROLL_DOWN.targetKey])
        assertEquals("ZR", right[MouseAction.RIGHT_CLICK.targetKey])
        assertEquals("RIGHT_STICK_UP", right[MouseAction.SCROLL_UP.targetKey])
    }

    @Test
    fun `the toggle combo is C, or Capture on a left Joy-Con`() {
        listOf(JoyconSide.DUAL, JoyconSide.RIGHT, JoyconSide.GAMECUBE).forEach { side ->
            assertEquals("$side", JoyconButton.Chat.name, applied(side)[MOUSE_MODE_KEY])
        }
        assertEquals(JoyconButton.Capture.name, applied(JoyconSide.LEFT)[MOUSE_MODE_KEY])
    }

    @Test
    fun `a layout's own mouse bindings win`() {
        val entries = MappingLayouts.entriesOf(Console.PC, JoyconSide.RIGHT, WasdMouseMapping)

        assertEquals("X", entries[MouseAction.SCROLL_UP.targetKey])
    }
}
