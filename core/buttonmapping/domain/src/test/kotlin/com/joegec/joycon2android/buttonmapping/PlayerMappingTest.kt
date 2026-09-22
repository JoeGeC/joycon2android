package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.left
import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.right
import com.joegec.joycon2android.buttonmapping.preset.MarioKartWiiMapping
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerMappingTest {

    private val fixture = MappingFixture()
    private val body = left()

    @Test
    fun `an untouched player reads as the layout they chose`() = runBlocking {
        fixture.applyLayout(fixture.console, body, MarioKartWiiMapping.id)

        assertEquals(MarioKartWiiMapping.displayName, fixture.playerMapping(body).layout?.displayName)
    }

    @Test
    fun `changing a binding turns it custom, and undoing that change turns it back`() = runBlocking {
        fixture.applyLayout(fixture.console, body, MarioKartWiiMapping.id)
        val original = MarioKartWiiMapping.entries(body.side).getValue("A")

        fixture.setMapping(fixture.console, body, "A", "Up")
        assertNull(fixture.playerMapping(body).layout)

        fixture.setMapping(fixture.console, body, "A", original)
        assertEquals(MarioKartWiiMapping.id, fixture.playerMapping(body).layout?.id)
    }

    @Test
    fun `the sideways-remote switch counts as a change of its own`() = runBlocking {
        fixture.applyLayout(fixture.console, body, MarioKartWiiMapping.id)

        fixture.setSidewaysRemote(fixture.console, body, false)

        assertNull(fixture.playerMapping(body).layout)
    }

    @Test
    fun `one player's change leaves the next player's mapping alone`() = runBlocking {
        val other = left(PlayerNumber.P2)
        fixture.applyLayout(fixture.console, body, MarioKartWiiMapping.id)
        fixture.applyLayout(fixture.console, other, WiiMapping.id)

        fixture.setMapping(fixture.console, body, "A", "Up")

        assertNull(fixture.playerMapping(body).layout)
        assertEquals(WiiMapping.id, fixture.playerMapping(other).layout?.id)
    }

    @Test
    fun `resetting puts the body back on the console's own layout`() = runBlocking {
        fixture.applyLayout(fixture.console, body, MarioKartWiiMapping.id)

        fixture.resetMapping(fixture.console, body)

        val mapping = fixture.playerMapping(body)
        assertEquals(WiiMapping.id, mapping.layout?.id)
        assertFalse(mapping.sidewaysRemote)
    }

    @Test
    fun `saving names what the player built and offers it to that body`() = runBlocking {
        fixture.applyLayout(fixture.console, body, MarioKartWiiMapping.id)
        fixture.setMapping(fixture.console, body, "A", "Up")

        fixture.saveCustomLayout(fixture.console, body, "My Wheel")

        val mapping = fixture.playerMapping(body)
        assertEquals("My Wheel", mapping.layout?.displayName)
        assertEquals("Up", mapping.entries["A"])
        assertTrue(fixture.layoutsFor(body.side).any { it.displayName == "My Wheel" })
    }

    @Test
    fun `a layout saved from one body is not offered to another`() = runBlocking {
        fixture.saveCustomLayout(fixture.console, body, "My Wheel")

        val other = fixture.layoutsFor(right().side)

        assertFalse(other.any { it.displayName == "My Wheel" })
    }

    @Test
    fun `deleting a layout keeps the bindings of everyone on it and only takes the name`() = runBlocking {
        fixture.setMapping(fixture.console, body, "A", "Up")
        fixture.saveCustomLayout(fixture.console, body, "My Wheel")
        val saved = fixture.savedLayoutNamed("My Wheel")

        fixture.deleteCustomLayout(saved.id)

        val mapping = fixture.playerMapping(body)
        assertNull(mapping.layout)
        assertEquals("Up", mapping.entries["A"])
    }

    @Test
    fun `saving the same layout again gives those bindings their name back`() = runBlocking {
        fixture.setMapping(fixture.console, body, "A", "Up")
        fixture.saveCustomLayout(fixture.console, body, "My Wheel")
        fixture.deleteCustomLayout(fixture.savedLayoutNamed("My Wheel").id)

        fixture.saveCustomLayout(fixture.console, body, "My Wheel")

        assertEquals("My Wheel", fixture.playerMapping(body).layout?.displayName)
    }

    @Test
    fun `a layout someone else saved names an identical mapping arrived at alone`() = runBlocking {
        val other = left(PlayerNumber.P2)
        fixture.setMapping(fixture.console, body, "A", "Up")
        fixture.saveCustomLayout(fixture.console, body, "My Wheel")

        fixture.setMapping(fixture.console, other, "A", "Up")

        assertNotNull(fixture.playerMapping(other).layout)
        assertEquals("My Wheel", fixture.playerMapping(other).layout?.displayName)
    }
}
