package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MouseOnlyMapping
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.MouseAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MouseOnlyMappingTest {

    private fun applied(side: JoyconSide) = MappingLayouts.entriesOf(Console.PC, side, MouseOnlyMapping)

    private fun mouse(side: JoyconSide) = applied(side).mouseActionBindings(side, sidewaysRemote = false).sources

    @Test
    fun `clears the keys WASD + mouse binds`() {
        JoyconSide.entries.forEach { side ->
            assertTrue(applied(side).keyboardKeyBindings(side, sidewaysRemote = false).sources.isEmpty())
        }
    }

    @Test
    fun `a pair and a right Joy-Con middle-click on RS`() {
        assertEquals(listOf(MappingSource.Button(JoyconButton.RS)), mouse(JoyconSide.DUAL)[MouseAction.MIDDLE_CLICK])
        assertEquals(listOf(MappingSource.Button(JoyconButton.RS)), mouse(JoyconSide.RIGHT)[MouseAction.MIDDLE_CLICK])
    }

    @Test
    fun `a left Joy-Con middle-clicks on LS and scrolls on its own stick`() {
        val left = mouse(JoyconSide.LEFT)

        assertEquals(listOf(MappingSource.Button(JoyconButton.LS)), left[MouseAction.MIDDLE_CLICK])
        assertEquals(listOf(MappingSource.Stick(StickSource.LEFT_STICK, StickDirection.UP)), left[MouseAction.SCROLL_UP])
    }
}
