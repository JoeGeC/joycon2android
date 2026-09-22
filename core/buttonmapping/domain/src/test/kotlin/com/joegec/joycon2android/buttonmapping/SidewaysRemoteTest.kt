package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.right
import com.joegec.joycon2android.buttonmapping.preset.MarioKartWiiMapping
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SidewaysRemoteTest {

    private val fixture = MappingFixture()
    private val body = right(PlayerNumber.P1)

    @Test
    fun `a body nothing has set follows the console's default layout`() = runBlocking {
        assertFalse(fixture.playerMapping(body).sidewaysRemote)
    }

    @Test
    fun `applying a layout takes its answer with it, either way`() = runBlocking {
        fixture.applyLayout(fixture.console, body, MarioKartWiiMapping.id)
        assertTrue(fixture.playerMapping(body).sidewaysRemote)

        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        assertFalse(fixture.playerMapping(body).sidewaysRemote)
    }

    @Test
    fun `the player's switch stands until a layout is applied over it`() = runBlocking {
        fixture.applyLayout(fixture.console, body, WiiMapping.id)

        fixture.setSidewaysRemote(fixture.console, body, true)
        assertTrue(fixture.playerMapping(body).sidewaysRemote)

        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        assertFalse(fixture.playerMapping(body).sidewaysRemote)
    }

    @Test
    fun `one player's switch leaves the others alone`() = runBlocking {
        val other = right(PlayerNumber.P2)

        fixture.setSidewaysRemote(fixture.console, body, true)

        assertTrue(fixture.playerMapping(body).sidewaysRemote)
        assertEquals(false, fixture.playerMapping(other).sidewaysRemote)
    }
}
