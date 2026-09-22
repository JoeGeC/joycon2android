package com.joegec.joycon2android.buttonmapping

import org.junit.Assert.assertEquals
import org.junit.Test

class MappingLayoutsTest {

    @Test
    fun `the first suggestion is the first number`() {
        assertEquals("Custom 1", MappingLayouts.nextName("Custom", emptyList()))
    }

    @Test
    fun `each suggestion counts past the names already taken`() {
        assertEquals("Custom 3", MappingLayouts.nextName("Custom", listOf("Custom 1", "Custom 2")))
    }

    @Test
    fun `a number freed by a deleted layout is suggested again`() {
        assertEquals("Custom 2", MappingLayouts.nextName("Custom", listOf("Custom 1", "Custom 3")))
    }

    @Test
    fun `names of the user's own choosing stand in nobody's way`() {
        assertEquals("Custom 1", MappingLayouts.nextName("Custom", listOf("My Wheel", "Customised")))
    }
}
