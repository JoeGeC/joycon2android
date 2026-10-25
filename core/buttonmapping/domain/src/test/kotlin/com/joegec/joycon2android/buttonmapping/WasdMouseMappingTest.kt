package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.buttonmapping.preset.WasdMouseMapping
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.KeyboardKey
import com.joegec.joycon2android.model.MouseAction
import org.junit.Assert.assertEquals
import org.junit.Test

class WasdMouseMappingTest {

    private val pair = WasdMouseMapping.entries(JoyconSide.DUAL)

    @Test
    fun `mouse and keyboard games start on WASD + mouse`() {
        assertEquals(WasdMouseMapping, MappingPresets.default(Console.PC))
    }

    @Test
    fun `the left stick walks and ZR clicks`() {
        val keys = pair.keyboardKeyBindings(JoyconSide.DUAL, sidewaysRemote = false).sources
        val mouse = pair.mouseActionBindings(JoyconSide.DUAL, sidewaysRemote = false).sources

        assertEquals(listOf(MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.UP)), keys[KeyboardKey.W])
        assertEquals(listOf(MappingSource.Button(JoyconButton.ZR)), mouse[MouseAction.LEFT_CLICK])
    }

    @Test
    fun `no button presses two keys`() {
        val buttons = pair.values.filter { value -> JoyconButton.entries.any { it.name == value } }

        assertEquals(buttons.size, buttons.toSet().size)
    }
}
