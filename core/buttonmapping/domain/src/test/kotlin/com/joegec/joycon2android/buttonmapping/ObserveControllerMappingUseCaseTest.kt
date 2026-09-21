package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MarioKartWiiMapping
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

    private class StoredPreset(private val presetId: String? = null) : MappingPresetRepository {
        override fun observe(console: Console): Flow<String?> = flowOf(presetId)
        override suspend fun set(console: Console, presetId: String) = Unit
    }

    private fun observe(
        stored: Map<String, String>,
        side: JoyconSide = JoyconSide.DUAL,
        console: Console = Console.GAMECUBE,
        presetId: String? = null,
    ) = runBlocking {
        ObserveControllerMappingUseCase(
            StoredMapping(stored),
            ObserveMappingPresetUseCase(StoredPreset(presetId)),
        )(console, side).first()
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
    fun `a source saved under the Capture button's former Camera name still binds Capture`() {
        val mapping = observe(mapOf("A" to "Camera"))

        assertEquals("Capture", mapping["A"])
    }

    @Test
    fun `the chosen preset supplies the defaults`() {
        val mapping = observe(emptyMap(), JoyconSide.RIGHT, Console.WIIMOTE_NUNCHUK, MarioKartWiiMapping.id)

        assertEquals("X", mapping["Two"])
        assertEquals("RIGHT_STICK_UP|SlRight", mapping["DPadUp"])
    }

    @Test
    fun `a preset id from another build falls back to the console's default`() {
        val mapping = observe(emptyMap(), JoyconSide.RIGHT, Console.WIIMOTE_NUNCHUK, "NO_SUCH_PRESET")

        assertEquals("B", mapping["Two"])
    }

    @Test
    fun `a direction set to None overrides its default`() {
        val mapping = observe(mapOf("CStick_LEFT" to ""))

        assertEquals("", mapping["CStick_LEFT"])
        assertEquals(null, MappingSource.fromId(mapping.getValue("CStick_LEFT")))
    }
}
