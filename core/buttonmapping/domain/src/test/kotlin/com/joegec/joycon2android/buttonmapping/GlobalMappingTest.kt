package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.left
import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.right
import com.joegec.joycon2android.buttonmapping.preset.MarioKartWiiMapping
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalMappingTest {

    private val fixture = MappingFixture()
    private val first = left()
    private val second = right()
    private val bodies = listOf(first, second)

    private suspend fun putBothOn(layoutId: String) =
        fixture.applyGlobalLayout(fixture.console, bodies, layoutId)

    private suspend fun savedSet() = fixture.globalMapping(first, second).savedLayouts.single()

    @Test
    fun `a layout every player reads as names the session`() = runBlocking {
        putBothOn(WiiMapping.id)

        assertEquals(WiiMapping.displayName, fixture.globalMapping(first, second).displayName)
    }

    @Test
    fun `one player changing leaves the session with no name of its own`() = runBlocking {
        putBothOn(WiiMapping.id)

        fixture.setMapping(fixture.console, first, "A", "Up")

        assertNull(fixture.globalMapping(first, second).displayName)
    }

    @Test
    fun `players on different layouts leave the session with no name of its own`() = runBlocking {
        fixture.applyLayout(fixture.console, first, MarioKartWiiMapping.id)
        fixture.applyLayout(fixture.console, second, WiiMapping.id)

        assertNull(fixture.globalMapping(first, second).displayName)
    }

    @Test
    fun `a saved set names the session again for as long as every player still matches it`() = runBlocking {
        putBothOn(WiiMapping.id)
        fixture.setMapping(fixture.console, first, "A", "Up")

        fixture.saveGlobalLayout(fixture.console, bodies, "Party")
        assertEquals("Party", fixture.globalMapping(first, second).displayName)

        fixture.setMapping(fixture.console, second, "A", "Down")
        assertNull(fixture.globalMapping(first, second).displayName)
    }

    @Test
    fun `a saved set fits only the players and bodies it was saved from`() = runBlocking {
        fixture.saveGlobalLayout(fixture.console, bodies, "Party")

        val saved = savedSet()

        assertTrue(saved.fits(listOf(second, first)))
        assertFalse(saved.fits(listOf(first)))
        assertFalse(saved.fits(listOf(first, PlayerBody(PlayerNumber.P2, JoyconSide.DUAL))))
    }

    @Test
    fun `a saved set names the bodies it wants, player by player`() = runBlocking {
        val three = bodies + PlayerBody(PlayerNumber.P3, JoyconSide.DUAL)
        fixture.saveGlobalLayout(fixture.console, three, "Party")

        assertEquals("P1 L, P2 R, P3 L/R", savedSet().playerSummary)
    }

    @Test
    fun `restoring a saved set gives every player back the bindings it froze`() = runBlocking {
        fixture.applyLayout(fixture.console, first, MarioKartWiiMapping.id)
        fixture.setMapping(fixture.console, first, "A", "Up")
        fixture.applyLayout(fixture.console, second, WiiMapping.id)
        fixture.saveGlobalLayout(fixture.console, bodies, "Party")
        val saved = savedSet()

        putBothOn(WiiMapping.id)
        fixture.applyGlobalLayout(fixture.console, bodies, saved.id)

        val restored = fixture.playerMapping(first)
        assertEquals("Up", restored.entries["A"])
        assertTrue(restored.sidewaysRemote)
        assertEquals(WiiMapping.id, fixture.playerMapping(second).layout?.id)
    }

    @Test
    fun `a set still restores a deleted layout's bindings, and names them again once it is back`() = runBlocking {
        fixture.setMapping(fixture.console, first, "A", "Up")
        fixture.saveCustomLayout(fixture.console, first, "My Wheel")
        fixture.saveGlobalLayout(fixture.console, bodies, "Party")
        val saved = savedSet()
        fixture.deleteCustomLayout(fixture.savedLayoutNamed("My Wheel").id)

        putBothOn(WiiMapping.id)
        fixture.applyGlobalLayout(fixture.console, bodies, saved.id)

        assertEquals("Up", fixture.playerMapping(first).entries["A"])
        assertNull(fixture.playerMapping(first).layout)

        fixture.saveCustomLayout(fixture.console, first, "My Wheel")

        assertEquals("My Wheel", fixture.playerMapping(first).layout?.displayName)
        assertEquals("Party", fixture.globalMapping(first, second).displayName)
    }
}
