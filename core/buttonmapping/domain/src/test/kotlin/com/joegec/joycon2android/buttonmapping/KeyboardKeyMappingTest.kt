package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.GameCubeMapping
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.KeyboardKey
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardKeyMappingTest {

    @Test
    fun `a keyboard key fires on any of its buttons and ignores stick directions`() {
        val entries = mapOf(KeyboardKey.W.targetKey to "Up|X|LEFT_STICK_UP")

        assertEquals(
            mapOf(KeyboardKey.W to setOf(JoyconButton.Up, JoyconButton.X)),
            entries.keyboardKeyBindings().buttons,
        )
    }

    @Test
    fun `keyboard and Android targets never read each other's entries`() {
        val entries = mapOf(KeyboardKey.ESCAPE.targetKey to "B")

        assertEquals(emptyMap<Any, Any>(), entries.androidKeyBindings().buttons)
    }

    @Test
    fun `a bound keyboard key makes the mapping custom`() {
        val entries = GameCubeMapping.entries(JoyconSide.DUAL) + (KeyboardKey.SPACE.targetKey to "A")

        assertEquals(null, MappingLayouts.matching(Console.GAMECUBE, JoyconSide.DUAL, entries, RemoteMotion(), emptyList()))
    }
}
