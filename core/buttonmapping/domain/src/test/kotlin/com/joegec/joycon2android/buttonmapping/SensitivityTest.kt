package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.MappingFixture.Companion.right
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SensitivityTest {

    private val fixture = MappingFixture()
    private val body = right(PlayerNumber.P1)

    private suspend fun sensitivity() = fixture.playerMapping(body).motion.sensitivityPercent

    @Test
    fun `a body nothing has set reads swings slightly boosted`() = runBlocking {
        assertEquals(110, sensitivity())
    }

    @Test
    fun `a dragged value snaps to the step and stays in range`() = runBlocking {
        fixture.setSensitivity(fixture.console, body, 123)
        assertEquals(125, sensitivity())

        fixture.setSensitivity(fixture.console, body, 400)
        assertEquals(200, sensitivity())

        fixture.setSensitivity(fixture.console, body, 10)
        assertEquals(50, sensitivity())
    }

    @Test
    fun `the slider stands until a layout is applied over it, and counts as a change`() = runBlocking {
        fixture.applyLayout(fixture.console, body, WiiMapping.id)

        fixture.setSensitivity(fixture.console, body, 120)
        assertEquals(120, sensitivity())
        assertNull(fixture.playerMapping(body).layout)

        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        assertEquals(110, sensitivity())
    }

    @Test
    fun `a saved layout brings its sensitivity back`() = runBlocking {
        fixture.setSensitivity(fixture.console, body, 115)
        fixture.saveCustomLayout(fixture.console, body, "Golf")
        val golf = fixture.savedLayoutNamed("Golf")

        fixture.applyLayout(fixture.console, body, WiiMapping.id)
        fixture.applyLayout(fixture.console, body, golf.id)

        assertEquals(115, sensitivity())
        assertEquals(golf.id, fixture.playerMapping(body).layout?.id)
    }
}
