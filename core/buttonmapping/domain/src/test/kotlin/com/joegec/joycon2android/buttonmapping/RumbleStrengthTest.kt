package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.left
import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.right
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RumbleStrengthTest {

    private val fixture = MappingFixture(Console.SWITCH_PRO)
    private val body = left(PlayerNumber.P1)

    private suspend fun rumble() = fixture.playerMapping(body).entries.rumblePercent

    @Test
    fun `a body nothing has set rumbles at a fifth of the safe maximum`() = runBlocking {
        assertEquals(20, rumble())
    }

    @Test
    fun `a dragged value snaps to the step and stays in range`() = runBlocking {
        fixture.setRumbleStrength(fixture.console, body, 23)
        assertEquals(25, rumble())

        fixture.setRumbleStrength(fixture.console, body, 400)
        assertEquals(100, rumble())

        fixture.setRumbleStrength(fixture.console, body, -5)
        assertEquals(0, rumble())
    }

    @Test
    fun `back at the default the body answers to its layout again`() = runBlocking {
        val layout = MappingPresets.default(fixture.console).id
        fixture.applyLayout(fixture.console, body, layout)

        fixture.setRumbleStrength(fixture.console, body, 50)
        assertNull(fixture.playerMapping(body).layout)

        fixture.setRumbleStrength(fixture.console, body, 20)
        assertEquals(layout, fixture.playerMapping(body).layout?.id)
    }

    @Test
    fun `a saved layout brings its rumble back and a shipped one resets it`() = runBlocking {
        fixture.setRumbleStrength(fixture.console, body, 60)
        fixture.saveCustomLayout(fixture.console, body, "Quiet")
        val quiet = fixture.savedLayoutNamed("Quiet")

        fixture.applyLayout(fixture.console, body, MappingPresets.default(fixture.console).id)
        assertEquals(20, rumble())

        fixture.applyLayout(fixture.console, body, quiet.id)
        assertEquals(60, rumble())
    }

    @Test
    fun `switching grip keeps the rumble`() = runBlocking {
        fixture.setRumbleStrength(fixture.console, body, 40)

        fixture.setProController(fixture.console, body, true)

        assertEquals(40, rumble())
    }

    @Test
    fun `each player's strength is read for the active bodies`() = runBlocking {
        val other = right(PlayerNumber.P2)
        fixture.setRumbleStrength(fixture.console, other, 30)

        val strengths = fixture.observeRumbleStrengths(fixture.console, listOf(body, other)).first()

        assertEquals(mapOf(PlayerNumber.P1 to 20, PlayerNumber.P2 to 30), strengths)
    }
}
