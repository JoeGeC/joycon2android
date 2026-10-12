package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.right
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThrustTest {

    private val fixture = MappingFixture()
    private val body = right(PlayerNumber.P1)

    @Test
    fun `a body nothing has set doesn't thrust`() = runBlocking {
        assertFalse(fixture.playerMapping(body).motion.thrust)
    }

    @Test
    fun `the player's switch stands until a layout is applied over it`() = runBlocking {
        fixture.applyLayout(fixture.console, body, WiiMapping.id)

        fixture.setThrust(fixture.console, body, true)
        assertTrue(fixture.playerMapping(body).motion.thrust)

        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        assertFalse(fixture.playerMapping(body).motion.thrust)
    }

    @Test
    fun `the switch counts as a change of its own`() = runBlocking {
        fixture.applyLayout(fixture.console, body, WiiMapping.id)

        fixture.setThrust(fixture.console, body, true)

        assertNull(fixture.playerMapping(body).layout)
    }

    @Test
    fun `a saved layout brings its thrust back`() = runBlocking {
        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        fixture.setThrust(fixture.console, body, true)
        fixture.saveCustomLayout(fixture.console, body, "Billiards")
        val billiards = fixture.savedLayoutNamed("Billiards")

        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        fixture.applyLayout(fixture.console, body, billiards.id)

        assertTrue(fixture.playerMapping(body).motion.thrust)
        assertEquals(billiards.id, fixture.playerMapping(body).layout?.id)
    }
}
