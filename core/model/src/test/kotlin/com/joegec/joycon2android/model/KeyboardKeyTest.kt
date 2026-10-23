package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardKeyTest {

    @Test
    fun `only A to Z are letters`() {
        assertEquals(('A'..'Z').map { it.toString() }, KeyboardKey.entries.filter { it.isLetter }.map { it.name })
    }
}
