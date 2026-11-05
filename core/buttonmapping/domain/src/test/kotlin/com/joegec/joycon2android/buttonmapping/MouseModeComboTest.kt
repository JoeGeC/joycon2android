package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MouseModeComboTest {

    private val combo = HoldCombo(listOf(MappingSource.Button(JoyconButton.Chat), MappingSource.Button(JoyconButton.RS)))

    private fun right(pressed: Set<String>, stickY: Int = 2048) = PlayerState(
        PlayerNumber.P1,
        right = ConnectedJoycon("r", Side.RIGHT, "R", input = JoyconInput(pressed = pressed, stickY = stickY)),
    )

    @Test
    fun `a combo holds only with every source down`() {
        assertTrue(combo.heldBy(right(setOf("Chat", "RS"))))
        assertFalse(combo.heldBy(right(setOf("Chat"))))
    }

    @Test
    fun `an empty combo is never held`() {
        assertFalse(HoldCombo(emptyList()).heldBy(right(emptySet())))
    }

    @Test
    fun `the mouse mode entry reads as one combo`() {
        val entries = mapOf(MOUSE_MODE_KEY to "Chat|RS")

        assertEquals(setOf(combo), entries.mouseModeCombos(JoyconSide.RIGHT, sidewaysRemote = false).sources.keys)
    }

    @Test
    fun `an unbound mouse mode has no combo`() {
        assertTrue(emptyMap<String, String>().mouseModeCombos(JoyconSide.RIGHT, sidewaysRemote = false).sources.isEmpty())
    }

    @Test
    fun `two consoles' different combos both stay whole`() {
        val other = mapOf(MOUSE_MODE_KEY to "Plus|R").mouseModeCombos(JoyconSide.RIGHT, sidewaysRemote = false)
        val merged = mapOf(MOUSE_MODE_KEY to "Chat|RS").mouseModeCombos(JoyconSide.RIGHT, sidewaysRemote = false) + other

        assertEquals(2, merged.sources.size)
    }

    @Test
    fun `a grip switch keeps the mouse mode combo`() {
        assertTrue(MOUSE_MODE_KEY in DEVICE_KEY_TARGETS)
    }

    @Test
    fun `every source held at once is read for a combo`() {
        assertEquals(
            setOf(MappingSource.Button(JoyconButton.Chat), MappingSource.Button(JoyconButton.RS)),
            right(setOf("Chat", "RS")).heldSources(JoyconSide.RIGHT, heldSideways = false),
        )
    }
}
