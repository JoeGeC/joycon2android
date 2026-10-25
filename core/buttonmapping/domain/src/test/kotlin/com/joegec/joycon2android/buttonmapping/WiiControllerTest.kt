package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.right
import com.joegec.joycon2android.buttonmapping.preset.ClassicControllerMapping
import com.joegec.joycon2android.buttonmapping.preset.MarioKartNunchukMapping
import com.joegec.joycon2android.buttonmapping.preset.MarioKartWheelMapping
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import com.joegec.joycon2android.buttonmapping.target.ClassicButton
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class WiiControllerTest {

    private val fixture = MappingFixture()
    private val body = right(PlayerNumber.P1)
    private val pair = PlayerBody(PlayerNumber.P1, JoyconSide.DUAL)

    private suspend fun controllerOf(body: PlayerBody) = fixture.playerMapping(body).motion.controller

    @Test
    fun `a body nothing has set follows the console's default layout`() = runBlocking {
        assertEquals(WiiController.REMOTE, controllerOf(body))
    }

    @Test
    fun `applying a layout takes its answer with it, either way`() = runBlocking {
        fixture.applyLayout(fixture.console, body, MarioKartWheelMapping.id)
        assertEquals(WiiController.SIDEWAYS_REMOTE, controllerOf(body))

        fixture.applyLayout(fixture.console, body, ClassicControllerMapping.id)
        assertEquals(WiiController.CLASSIC, controllerOf(body))

        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        assertEquals(WiiController.REMOTE, controllerOf(body))
    }

    @Test
    fun `the player's choice stands until a layout is applied over it`() = runBlocking {
        fixture.applyLayout(fixture.console, body, WiiMapping.id)

        fixture.setWiiController(fixture.console, body, WiiController.SIDEWAYS_REMOTE)
        assertEquals(WiiController.SIDEWAYS_REMOTE, controllerOf(body))

        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        assertEquals(WiiController.REMOTE, controllerOf(body))
    }

    @Test
    fun `one player's choice leaves the others alone`() = runBlocking {
        val other = right(PlayerNumber.P2)

        fixture.setWiiController(fixture.console, body, WiiController.SIDEWAYS_REMOTE)

        assertEquals(WiiController.SIDEWAYS_REMOTE, controllerOf(body))
        assertEquals(WiiController.REMOTE, controllerOf(other))
    }

    @Test
    fun `a first Classic Controller is bound as its layout, which it then reads back as`() = runBlocking {
        fixture.applyLayout(fixture.console, body, WiiMapping.id)

        fixture.setWiiController(fixture.console, body, WiiController.CLASSIC)

        val mapping = fixture.playerMapping(body)
        assertEquals("X", mapping.entries[ClassicButton.ClassicA.name])
        assertEquals(ClassicControllerMapping.id, mapping.layout?.id)
    }

    @Test
    fun `going back to the remote keeps its buttons, and Classic keeps its own`() = runBlocking {
        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        fixture.setWiiController(fixture.console, body, WiiController.CLASSIC)
        fixture.setMapping(fixture.console, body, ClassicButton.ClassicA.name, "Y")

        fixture.setWiiController(fixture.console, body, WiiController.REMOTE)
        fixture.setWiiController(fixture.console, body, WiiController.CLASSIC)

        val mapping = fixture.playerMapping(body)
        assertEquals("Y", mapping.entries[ClassicButton.ClassicA.name])
        assertEquals(WiiMapping.entries(JoyconSide.RIGHT)["A"], mapping.entries["A"])
    }

    @Test
    fun `a pair can't turn sideways, so a sideways layout plays it as a remote and still reads back`() = runBlocking {
        fixture.applyLayout(fixture.console, pair, MarioKartNunchukMapping.id)

        val mapping = fixture.playerMapping(pair)
        assertEquals(WiiController.REMOTE, mapping.motion.controller)
        assertEquals(MarioKartNunchukMapping.id, mapping.layout?.id)
    }
}
