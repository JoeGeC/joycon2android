package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MarioKartWheelMapping
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveControllerMappingUseCaseTest {

    private fun observe(
        stored: Map<String, String>,
        side: JoyconSide = JoyconSide.DUAL,
        console: Console = Console.GAMECUBE,
    ) = runBlocking {
        val body = PlayerBody(PlayerNumber.P1, side)
        val mappings = FakeControllerMappings()
        stored.forEach { (target, source) -> mappings.set(console, body, target, source) }
        ObserveControllerMappingUseCase(mappings)(console, body).first()
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
    fun `a direction set to None overrides its default`() {
        val mapping = observe(mapOf("CStick_LEFT" to ""))

        assertEquals("", mapping["CStick_LEFT"])
        assertEquals(null, MappingSource.fromId(mapping.getValue("CStick_LEFT")))
    }

    @Test
    fun `applying a layout writes out everything it says, so the layout is no longer needed`() = runBlocking {
        val fixture = MappingFixture()
        val body = MappingFixture.right(PlayerNumber.P1)

        fixture.applyLayout(fixture.console, body, MarioKartWheelMapping.id)

        val mapping = fixture.playerMapping(body)
        assertEquals("X", mapping.entries["Two"])
        assertEquals("RIGHT_STICK_UP|SlRight", mapping.entries["DPadUp"])
    }
}
