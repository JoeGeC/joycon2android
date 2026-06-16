package com.joegec.joycon2android.gamepad.emulator

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DolphinGcpadConfigTest {

    private fun joycon(side: Side) = ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con")

    @Test
    fun `device path uses the per-player virtual gamepad name`() {
        val result = DolphinGcpadConfig.merge(null, listOf(PlayerState(PlayerNumber.P2, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("[GCPad2]"))
        assertTrue(result.contains("Device = Android/1/Joy-Con Virtual Gamepad 2"))
    }

    @Test
    fun `device id is the enumeration rank, not the player number, when a slot is skipped`() {
        val players = listOf(
            PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)),
            PlayerState(PlayerNumber.P2, right = joycon(Side.RIGHT)),
            PlayerState(PlayerNumber.P4, right = joycon(Side.RIGHT)),
        )

        val result = DolphinGcpadConfig.merge(null, players)

        // P4 is the 3rd active pad → Android/3, but its port and name stay 4
        assertTrue(result.contains("[GCPad4]"))
        assertTrue(result.contains("Device = Android/3/Joy-Con Virtual Gamepad 4"))
    }

    @Test
    fun `a pair maps both sticks and the full button set`() {
        val both = PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

        val result = DolphinGcpadConfig.merge(null, listOf(both))

        assertTrue(result.contains("Buttons/A = `Button A`"))
        assertTrue(result.contains("Main Stick/Up = `Axis 14-`")) // right stick
        assertTrue(result.contains("C-Stick/Up = `Axis 1-`"))     // left stick
        assertTrue(result.contains("D-Pad/Up = `Axis 16-`"))
    }

    @Test
    fun `left-only maps the d-pad to the face buttons`() {
        val result = DolphinGcpadConfig.merge(null, listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT))))

        assertTrue(result.contains("Buttons/A = `Axis 15+`"))
        assertTrue(result.contains("Buttons/Start = `Button L2`"))
    }

    @Test
    fun `pro controllers are skipped`() {
        val result = DolphinGcpadConfig.merge(null, listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.PRO))))

        assertFalse(result.contains("[GCPad1]"))
    }

    @Test
    fun `core config sets each configured port to a standard controller, preserving other keys`() {
        val existing = "[Core]\nGFXBackend = Vulkan\nSIDevice0 = 0\n[Interface]\nFoo = Bar\n"
        val players = listOf(
            PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)),
            PlayerState(PlayerNumber.P4, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)),
        )

        val result = DolphinGcpadConfig.mergeCore(existing, players)

        assertTrue(result.contains("SIDevice0 = 6")) // replaced
        assertTrue(result.contains("SIDevice3 = 6")) // appended for P4's port
        assertTrue(result.contains("GFXBackend = Vulkan")) // untouched
        assertTrue(result.contains("[Interface]"))
        assertTrue(result.contains("Foo = Bar"))
    }

    @Test
    fun `core config creates a Core section when none exists`() {
        val result = DolphinGcpadConfig.mergeCore(null, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("[Core]"))
        assertTrue(result.contains("SIDevice0 = 6"))
    }

    @Test
    fun `unrelated sections are preserved`() {
        val existing = "[GCPad4]\nDevice = Foo\n"

        val result = DolphinGcpadConfig.merge(existing, listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT))))

        assertTrue(result.contains("[GCPad4]"))
        assertTrue(result.contains("Device = Foo"))
        assertTrue(result.contains("[GCPad1]"))
    }
}
