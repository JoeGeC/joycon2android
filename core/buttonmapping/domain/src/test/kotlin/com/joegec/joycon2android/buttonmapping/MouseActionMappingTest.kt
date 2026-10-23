package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.MouseAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MouseActionMappingTest {

    @Test
    fun `a mouse action fires on its buttons and stick directions`() {
        val entries = mapOf(MouseAction.SCROLL_UP.targetKey to "ZR|RIGHT_STICK_UP")

        assertEquals(
            mapOf(MouseAction.SCROLL_UP to listOf(MappingSource.Button(JoyconButton.ZR), MappingSource.Stick(StickSource.RIGHT_STICK, StickDirection.UP))),
            entries.mouseActionBindings(JoyconSide.DUAL, sidewaysRemote = false).sources,
        )
    }

    @Test
    fun `a grip switch keeps mouse actions`() {
        assertTrue(MouseAction.entries.all { it.targetKey in DEVICE_KEY_TARGETS })
    }
}
