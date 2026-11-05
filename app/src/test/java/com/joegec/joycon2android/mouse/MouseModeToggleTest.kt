package com.joegec.joycon2android.mouse

import com.joegec.joycon2android.androidkeys.OutputBindings
import com.joegec.joycon2android.buttonmapping.HoldCombo
import com.joegec.joycon2android.buttonmapping.KeyBindings
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class MouseModeToggleTest {

    private val combo = HoldCombo(listOf(MappingSource.Button(JoyconButton.Chat), MappingSource.Button(JoyconButton.RS)))
    private val acting = MutableStateFlow(true)
    private val bindings = MutableStateFlow(mapOf(PlayerNumber.P1 to KeyBindings(mapOf(combo to combo.sources))))
    private var now = 0L
    private val toggled = mutableListOf<PlayerNumber>()
    private val toggle = MouseModeToggle(OutputBindings(acting, bindings), { now }) { toggled += it.player }

    private fun player(vararg pressed: JoyconButton) = PlayerState(
        PlayerNumber.P1,
        right = ConnectedJoycon("r", Side.RIGHT, "R", input = JoyconInput(pressed = pressed.map { it.id }.toSet())),
    )

    private fun holdFor(millis: Long, vararg pressed: JoyconButton) {
        toggle.onFrame(listOf(player(*pressed)))
        now += millis
        toggle.onFrame(listOf(player(*pressed)))
    }

    @Test
    fun `holding the combo for a second switches the mouse off`() {
        holdFor(MouseModeToggle.HOLD_MILLIS, JoyconButton.Chat, JoyconButton.RS)

        assertEquals(setOf(PlayerNumber.P1), toggle.off.value)
        assertEquals(listOf(PlayerNumber.P1), toggled)
    }

    @Test
    fun `a shorter hold does nothing`() {
        holdFor(MouseModeToggle.HOLD_MILLIS - 1, JoyconButton.Chat, JoyconButton.RS)

        assertEquals(emptySet<PlayerNumber>(), toggle.off.value)
    }

    @Test
    fun `half the combo never toggles`() {
        holdFor(MouseModeToggle.HOLD_MILLIS * 3, JoyconButton.Chat)

        assertEquals(emptySet<PlayerNumber>(), toggle.off.value)
    }

    @Test
    fun `holding on toggles once, and the next hold switches it back`() {
        holdFor(MouseModeToggle.HOLD_MILLIS, JoyconButton.Chat, JoyconButton.RS)
        holdFor(MouseModeToggle.HOLD_MILLIS, JoyconButton.Chat, JoyconButton.RS)
        assertEquals(setOf(PlayerNumber.P1), toggle.off.value)

        toggle.onFrame(listOf(player()))
        holdFor(MouseModeToggle.HOLD_MILLIS, JoyconButton.Chat, JoyconButton.RS)

        assertEquals(emptySet<PlayerNumber>(), toggle.off.value)
    }

    @Test
    fun `nothing toggles while device keys are paused`() {
        acting.value = false

        holdFor(MouseModeToggle.HOLD_MILLIS, JoyconButton.Chat, JoyconButton.RS)

        assertEquals(emptySet<PlayerNumber>(), toggle.off.value)
    }

    @Test
    fun `a player who leaves gets their mouse back next time`() {
        holdFor(MouseModeToggle.HOLD_MILLIS, JoyconButton.Chat, JoyconButton.RS)

        toggle.onFrame(emptyList())

        assertEquals(emptySet<PlayerNumber>(), toggle.off.value)
    }
}
