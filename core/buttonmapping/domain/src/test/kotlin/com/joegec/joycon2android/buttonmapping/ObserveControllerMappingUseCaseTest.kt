package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveControllerMappingUseCaseTest {

    private class StoredMapping(private val stored: Map<String, String>) : ControllerMappingRepository {
        override fun observe(console: Console, side: JoyconSide): Flow<Map<String, String>> = flowOf(stored)
        override suspend fun set(console: Console, side: JoyconSide, targetKey: String, sourceId: String) = Unit
        override suspend fun clear(console: Console, side: JoyconSide) = Unit
    }

    private fun observe(stored: Map<String, String>, side: JoyconSide = JoyconSide.DUAL) = runBlocking {
        ObserveControllerMappingUseCase(StoredMapping(stored))(Console.GAMECUBE, side).first()
    }

    @Test
    fun `a pair's sticks default to following the physical sticks direction by direction`() {
        val mapping = observe(emptyMap())

        assertEquals("LEFT_STICK_UP", mapping["MainStick_UP"])
        assertEquals("RIGHT_STICK_RIGHT", mapping["CStick_RIGHT"])
    }

    @Test
    fun `a lone Joy-Con's main stick defaults to its own stick`() {
        val mapping = observe(emptyMap(), JoyconSide.RIGHT)

        assertEquals("RIGHT_STICK_DOWN", mapping["MainStick_DOWN"])
    }

    @Test
    fun `a whole-stick route saved by an older version expands into its four directions`() {
        val mapping = observe(mapOf("MainStick" to "RIGHT_STICK"))

        assertEquals("RIGHT_STICK_UP", mapping["MainStick_UP"])
        assertEquals("RIGHT_STICK_LEFT", mapping["MainStick_LEFT"])
    }

    @Test
    fun `a direction set on its own wins over an older whole-stick route`() {
        val mapping = observe(mapOf("MainStick" to "RIGHT_STICK", "MainStick_UP" to "A"))

        assertEquals("A", mapping["MainStick_UP"])
        assertEquals("RIGHT_STICK_DOWN", mapping["MainStick_DOWN"])
    }

    @Test
    fun `a direction set to None overrides its default`() {
        val mapping = observe(mapOf("CStick_LEFT" to ""))

        assertEquals("", mapping["CStick_LEFT"])
        assertEquals(null, MappingSource.fromId(mapping.getValue("CStick_LEFT")))
    }
}
