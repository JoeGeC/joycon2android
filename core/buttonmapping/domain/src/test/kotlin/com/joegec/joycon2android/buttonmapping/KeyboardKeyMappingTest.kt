package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.GameCubeMapping
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.KeyboardKey
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardKeyMappingTest {

    @Test
    fun `a keyboard key fires on its buttons and stick directions`() {
        val entries = mapOf(KeyboardKey.W.targetKey to "Up|LEFT_STICK_UP")

        assertEquals(
            mapOf(KeyboardKey.W to listOf(MappingSource.Button(JoyconButton.Up), MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.UP))),
            entries.keyboardKeyBindings(JoyconSide.DUAL, sidewaysRemote = false).sources,
        )
    }

    @Test
    fun `off a sideways remote a lone Joy-Con's up stays its own up`() {
        val entries = mapOf(KeyboardKey.W.targetKey to "LEFT_STICK_UP")

        val source = entries.keyboardKeyBindings(JoyconSide.LEFT, sidewaysRemote = false).sources.getValue(KeyboardKey.W)

        assertEquals(listOf(MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.UP)), source)
    }

    @Test
    fun `on a sideways remote up is towards the rail, the hardware's right on a left Joy-Con`() {
        val entries = mapOf(KeyboardKey.W.targetKey to "LEFT_STICK_UP")

        val source = entries.keyboardKeyBindings(JoyconSide.LEFT, sidewaysRemote = true).sources.getValue(KeyboardKey.W)

        assertEquals(listOf(MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.RIGHT)), source)
    }

    @Test
    fun `an Android key ignores stick directions`() {
        val entries = mapOf(com.joegec.joycon2android.model.AndroidKey.HOME.targetKey to "LEFT_STICK_UP")

        assertEquals(emptyMap<Any, Any>(), entries.androidKeyBindings().sources)
    }

    @Test
    fun `keyboard and Android targets never read each other's entries`() {
        val entries = mapOf(KeyboardKey.ESCAPE.targetKey to "B")

        assertEquals(emptyMap<Any, Any>(), entries.androidKeyBindings().sources)
    }

    @Test
    fun `a bound keyboard key makes the mapping custom`() {
        val entries = GameCubeMapping.entries(JoyconSide.DUAL) + (KeyboardKey.SPACE.targetKey to "A")

        assertEquals(null, MappingLayouts.matching(Console.GAMECUBE, JoyconSide.DUAL, entries, RemoteMotion(), emptyList()))
    }
}
