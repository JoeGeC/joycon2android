package com.joegec.joycon2android.mouse

import com.joegec.joycon2android.gamepad.mouse.MappedMouse
import com.joegec.joycon2android.gamepad.mouse.MouseButton
import com.joegec.joycon2android.gamepad.mouse.MouseFrame
import com.joegec.joycon2android.model.MouseAction
import org.junit.Assert.assertEquals
import org.junit.Test

class MouseMixTest {

    private val pushed = mutableListOf<MouseFrame>()
    private val mix = MouseMix(MappedMouse { 0L }) { pushed += it }

    @Test
    fun `the desk mouse and the mapping share one report`() {
        mix.desk(MouseFrame(dx = 12, buttons = setOf(MouseButton.PRIMARY)))
        mix.send(setOf(MouseAction.RIGHT_CLICK))

        assertEquals(MouseFrame(dx = 12, buttons = setOf(MouseButton.PRIMARY, MouseButton.SECONDARY)), pushed.single())
    }

    @Test
    fun `a desk frame is sent once, not again on the next packet`() {
        mix.desk(MouseFrame(dx = 12))
        mix.send(emptySet())
        mix.send(emptySet())

        assertEquals(listOf(MouseFrame(dx = 12), MouseFrame()), pushed)
    }
}
