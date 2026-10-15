package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.SwitchProMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProSidewaysMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProSidewaysTriggersMapping
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProControllerTest {

    private val fixture = MappingFixture(Console.SWITCH_PRO)
    private val body = MappingFixture.left()

    @Test
    fun `a lone Joy-Con starts as Eden's single Joy-Con on the Standard layout`() = runBlocking {
        val mapping = fixture.playerMapping(body)

        assertFalse(mapping.entries.proController)
        assertTrue(mapping.entries.isSingleJoycon(Console.SWITCH_PRO, JoyconSide.LEFT))
        assertEquals(SwitchProMapping, mapping.layout)
    }

    @Test
    fun `turning on Pro Controller swaps to the sideways layout and keeps the Android keys`() = runBlocking {
        fixture.setMapping(Console.SWITCH_PRO, body, AndroidKey.HOME.targetKey, "Capture")
        fixture.setMapping(Console.SWITCH_PRO, body, "DPadUp", "Down")

        fixture.setProController(Console.SWITCH_PRO, body, true)

        val mapping = fixture.playerMapping(body)
        assertTrue(mapping.entries.proController)
        assertEquals("Capture", mapping.entries[AndroidKey.HOME.targetKey])
        assertEquals("", mapping.entries["DPadUp"])
        assertEquals("Down", mapping.entries["A"])
        assertEquals(null, mapping.layout)
    }

    @Test
    fun `turning it back off restores the Standard layout`() = runBlocking {
        fixture.setProController(Console.SWITCH_PRO, body, true)
        assertEquals(SwitchProSidewaysMapping, fixture.playerMapping(body).layout)

        fixture.setProController(Console.SWITCH_PRO, body, false)

        assertEquals(SwitchProMapping, fixture.playerMapping(body).layout)
    }

    @Test
    fun `the ZL-ZR sideways layout puts SL and SR on the triggers instead of the shoulders`() = runBlocking {
        fixture.applyLayout(Console.SWITCH_PRO, body, SwitchProSidewaysTriggersMapping.id)

        val mapping = fixture.playerMapping(body)
        assertTrue(mapping.entries.proController)
        assertEquals("SlLeft", mapping.entries["ZL"])
        assertEquals("SrLeft", mapping.entries["ZR"])
        assertEquals("", mapping.entries["L"])
        assertEquals("", mapping.entries["R"])
        assertEquals(SwitchProSidewaysTriggersMapping, mapping.layout)
    }

    @Test
    fun `a saved layout keeps the switch`() = runBlocking {
        fixture.setProController(Console.SWITCH_PRO, body, true)
        fixture.saveCustomLayout(Console.SWITCH_PRO, body, "Sideways")
        fixture.applyLayout(Console.SWITCH_PRO, body, SwitchProMapping.id)

        fixture.applyLayout(Console.SWITCH_PRO, body, fixture.savedLayoutNamed("Sideways").id)

        assertTrue(fixture.playerMapping(body).entries.proController)
    }

    @Test
    fun `only lone bodies Eden sees as a Joy-Con are single`() = runBlocking {
        fixture.setProController(Console.SWITCH_PRO, body, true)

        val single = fixture.observeSingleJoyconBodies().first()

        assertFalse(body in single)
        assertTrue(MappingFixture.right(PlayerNumber.P1) in single)
        assertFalse(single.any { !it.side.isLone })
    }
}
