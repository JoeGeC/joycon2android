package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.left
import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.right
import com.joegec.joycon2android.buttonmapping.preset.MarioKartNunchukMapping
import com.joegec.joycon2android.buttonmapping.preset.MarioKartWheelMapping
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

    // The domain says what a session agrees on; naming it is presentation's, so this stands in.
    private fun GlobalMapping.agreedName(): String? =
        matchingSaved?.name ?: sharedLayout?.id ?: sharedFamily?.name

    private suspend fun savedSet() = fixture.globalMapping(first, second).savedLayouts.single()

    @Test
    fun `a layout every player reads as names the session`() = runBlocking {
        putBothOn(WiiMapping.id)

        assertEquals(WiiMapping.id, fixture.globalMapping(first, second).agreedName())
    }

    @Test
    fun `one player changing leaves the session with no name of its own`() = runBlocking {
        putBothOn(WiiMapping.id)

        fixture.setMapping(fixture.console, first, "A", "Up")

        assertNull(fixture.globalMapping(first, second).agreedName())
    }

    @Test
    fun `players on different layouts leave the session with no name of its own`() = runBlocking {
        fixture.applyLayout(fixture.console, first, MarioKartWheelMapping.id)
        fixture.applyLayout(fixture.console, second, WiiMapping.id)

        assertNull(fixture.globalMapping(first, second).agreedName())
    }

    @Test
    fun `a saved set names the session again for as long as every player still matches it`() = runBlocking {
        putBothOn(WiiMapping.id)
        fixture.setMapping(fixture.console, first, "A", "Up")

        fixture.saveGlobalLayout(fixture.console, bodies, "Party")
        assertEquals("Party", fixture.globalMapping(first, second).agreedName())

        fixture.setMapping(fixture.console, second, "A", "Down")
        assertNull(fixture.globalMapping(first, second).agreedName())
    }

    // A table rarely holds the same thing, so the grip each body can be held in is what it gets.
    @Test
    fun `setting a grip nobody but a lone Joy-Con has gives a pair the other grip of the same game`() = runBlocking {
        val pair = PlayerBody(PlayerNumber.P3, JoyconSide.DUAL)
        val mixed = bodies + pair

        fixture.applyGlobalLayout(fixture.console, mixed, MarioKartWheelMapping.id)

        assertEquals(MarioKartWheelMapping.id, fixture.playerMapping(first).layout?.id)
        assertEquals(MarioKartNunchukMapping.id, fixture.playerMapping(pair).layout?.id)
    }

    @Test
    fun `players on two grips of one game still name the session`() = runBlocking {
        val pair = PlayerBody(PlayerNumber.P3, JoyconSide.DUAL)
        val mixed = bodies + pair

        fixture.applyGlobalLayout(fixture.console, mixed, MarioKartWheelMapping.id)

        assertEquals(LayoutFamily.MARIO_KART.name, fixture.globalMapping(first, second, pair).agreedName())
    }

    @Test
    fun `a saved set fits only the players and bodies it was saved from`() = runBlocking {
        fixture.saveGlobalLayout(fixture.console, bodies, "Party")

        val saved = savedSet()

        assertTrue(saved.fits(listOf(second, first)))
        assertFalse(saved.fits(listOf(first)))
        assertFalse(saved.fits(listOf(first, PlayerBody(PlayerNumber.P2, JoyconSide.DUAL))))
    }

    // Which bodies, in which order — what they are *called* is presentation's, so it is not here.
    @Test
    fun `a saved set records the bodies it wants, player by player`() = runBlocking {
        val three = bodies + PlayerBody(PlayerNumber.P3, JoyconSide.DUAL)
        fixture.saveGlobalLayout(fixture.console, three, "Party")

        assertEquals(three, savedSet().bodies.map { it.body })
    }

    @Test
    fun `restoring a saved set gives every player back the bindings it froze`() = runBlocking {
        fixture.applyLayout(fixture.console, first, MarioKartWheelMapping.id)
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

        assertEquals("My Wheel", (fixture.playerMapping(first).layout as? SavedLayout)?.name)
        assertEquals("Party", fixture.globalMapping(first, second).agreedName())
    }
}
