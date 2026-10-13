package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class GameCubeButtonsTest {

    @Test
    fun `a gamecube controller has no minus, stick clicks or back paddles`() {
        listOf(JoyconButton.Minus, JoyconButton.LS, JoyconButton.RS, JoyconButton.GL, JoyconButton.GR).forEach {
            assertFalse("$it", it in GameCubeButtons.present)
        }
    }

    @Test
    fun `its ZR is marked Z and every other button keeps its marking`() {
        assertEquals("Z", GameCubeButtons.marking(JoyconButton.ZR))
        assertNull(GameCubeButtons.marking(JoyconButton.ZL))
    }
}
