package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private fun defaultSwitchProMapping(side: JoyconSide) = MappingPresets.default(Console.SWITCH_PRO).entries(side)

class EdenDsuConfigTest {

    private fun joycon(side: Side) = ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con")

    private fun pair(player: PlayerNumber) =
        PlayerState(player, left = joycon(Side.LEFT), right = joycon(Side.RIGHT))

    private fun solo(player: PlayerNumber) = PlayerState(player, right = joycon(Side.RIGHT))

    private fun pro(player: PlayerNumber) = PlayerState(player, left = joycon(Side.PRO))

    private fun valueOf(config: String, key: String) =
        config.lines().first { it.substringBefore('=').trim() == key }.substringAfter('=').trim()

    private fun merge(existing: String?, players: List<PlayerState>) =
        EdenDsuConfig.merge(existing, players, ::defaultSwitchProMapping)

    private fun device(pad: Int) =
        "engine:cemuhookudp,guid:0000000000000000000000007f000001,port:${DsuConfig.PORT},pad:$pad"

    private fun binding(pad: Int) = "\"${device(pad)},motion:0\""

    @Test
    fun `a fresh config gains the controls section and the server switches`() {
        val result = merge(null, listOf(solo(PlayerNumber.P1)))

        assertTrue(result.contains("[Controls]"))
        assertEquals("true", valueOf(result, "motion_enabled"))
        assertEquals("true", valueOf(result, "enable_udp_controller"))
        assertEquals("127.0.0.1:${DsuConfig.PORT}", valueOf(result, "udp_input_servers"))
    }

    @Test
    fun `every written key pins its default flag off`() {
        val result = merge(null, listOf(solo(PlayerNumber.P1)))

        listOf("motion_enabled", "enable_udp_controller", "udp_input_servers", "player_0_motionright")
            .forEach { assertEquals("false for $it", "false", valueOf(result, "$it\\default")) }
    }

    @Test
    fun `a single joycon binds both motion slots to its own pad`() {
        val result = merge(null, listOf(solo(PlayerNumber.P2)))

        assertEquals(binding(1), valueOf(result, "player_1_motionright"))
        assertEquals(binding(1), valueOf(result, "player_1_motionleft"))
    }

    @Test
    fun `a pair splits its hands across the two pads it streams on`() {
        val result = merge(null, listOf(pair(PlayerNumber.P1)))

        assertEquals(binding(0), valueOf(result, "player_0_motionright"))
        assertEquals(binding(3), valueOf(result, "player_0_motionleft"))
    }

    @Test
    fun `a pair left without a second slot falls back to its own pad`() {
        val players = listOf(pair(PlayerNumber.P1), solo(PlayerNumber.P2), solo(PlayerNumber.P3), solo(PlayerNumber.P4))

        val result = merge(null, players)

        assertEquals(binding(0), valueOf(result, "player_0_motionright"))
        assertEquals(binding(0), valueOf(result, "player_0_motionleft"))
    }

    @Test
    fun `a pro controller is bound like any other single motion source`() {
        val result = merge(null, listOf(pro(PlayerNumber.P1)))

        assertEquals(binding(0), valueOf(result, "player_0_motionright"))
        assertEquals(binding(0), valueOf(result, "player_0_motionleft"))
    }

    @Test
    fun `players past the fourth slot get no binding`() {
        val result = merge(null, listOf(solo(PlayerNumber.P5)))

        assertFalse(result.contains("player_4_motion"))
    }

    @Test
    fun `a stale binding is dropped when its player no longer holds a controller`() {
        val existing = merge(null, listOf(solo(PlayerNumber.P1), solo(PlayerNumber.P2)))

        val result = merge(existing, listOf(solo(PlayerNumber.P1)))

        assertTrue(result.contains("player_0_motionright"))
        assertFalse(result.contains("player_1_motion"))
    }

    @Test
    fun `an existing udp server is kept and ours appended once`() {
        val existing = "[Controls]\nudp_input_servers=192.168.0.5:26760"

        val once = merge(existing, listOf(solo(PlayerNumber.P1)))
        val twice = merge(once, listOf(solo(PlayerNumber.P1)))

        assertEquals("192.168.0.5:26760,127.0.0.1:${DsuConfig.PORT}", valueOf(once, "udp_input_servers"))
        assertEquals(valueOf(once, "udp_input_servers"), valueOf(twice, "udp_input_servers"))
    }

    @Test
    fun `a player gets a whole controller, not just motion`() {
        val result = merge(null, listOf(pair(PlayerNumber.P1)))

        assertEquals("1", valueOf(result, "player_0_type"))
        assertEquals("true", valueOf(result, "player_0_connected"))
        // A -> Circle (0x2000), B -> Cross (0x4000), ZL -> L2 (0x100), Minus -> Share (0x1)
        assertEquals("\"${device(0)},button:8192\"", valueOf(result, "player_0_button_a"))
        assertEquals("\"${device(0)},button:16384\"", valueOf(result, "player_0_button_b"))
        assertEquals("\"${device(0)},button:256\"", valueOf(result, "player_0_button_zl"))
        assertEquals("\"${device(0)},button:1\"", valueOf(result, "player_0_button_minus"))
        // Home and the touchpad click ride the bytes above the two button bytes.
        assertEquals("\"${device(0)},button:262144\"", valueOf(result, "player_0_button_home"))
        assertEquals("\"${device(0)},button:524288\"", valueOf(result, "player_0_button_screenshot"))
        // Both sticks, in the order the pad packet carries them.
        assertEquals("\"${device(0)},axis_x:0,axis_y:1\"", valueOf(result, "player_0_lstick"))
        assertEquals("\"${device(0)},axis_x:2,axis_y:3\"", valueOf(result, "player_0_rstick"))
    }

    @Test
    fun `buttons and sticks all ride the player's own pad, only motion splits`() {
        val result = merge(null, listOf(pair(PlayerNumber.P1)))

        listOf("button_a", "button_y", "lstick", "rstick").forEach {
            assertTrue("$it on pad 0", valueOf(result, "player_0_$it").contains("pad:0,"))
        }
        assertEquals(binding(0), valueOf(result, "player_0_motionright"))
        assertEquals(binding(3), valueOf(result, "player_0_motionleft"))
    }

    @Test
    fun `a sideways joycon binds its rotated faces and its one stick`() {
        val result = merge(null, listOf(solo(PlayerNumber.P1)))

        // A sideways right Joy-Con's faces are rotated, so the Pro A target lands on a different bit.
        assertTrue(result.contains("player_0_button_a="))
        assertEquals("\"${device(0)},axis_x:0,axis_y:1\"", valueOf(result, "player_0_lstick"))
        assertFalse(result.contains("player_0_rstick="))
    }

    @Test
    fun `a stick direction driven by a button turns the stick digital, and a button can read a tilt`() {
        val mapping = defaultSwitchProMapping(JoyconSide.DUAL) + mapOf("RStick_UP" to "X", "A" to "LEFT_STICK_UP")

        val result = EdenDsuConfig.merge(null, listOf(pair(PlayerNumber.P1))) { mapping }

        fun nested(input: String) = "${device(0)},$input".replace(":", "$0").replace(",", "$1")
        val expected = "engine:analog_from_button,up:${nested("button:4096")}," +
            "down:${nested("axis:3,threshold:0.5,invert:-")}," +
            "left:${nested("axis:2,threshold:0.5,invert:-")}," +
            "right:${nested("axis:2,threshold:0.5,invert:+")}"
        assertEquals("\"$expected\"", valueOf(result, "player_0_rstick"))
        assertEquals("\"${device(0)},axis:1,threshold:0.5,invert:+\"", valueOf(result, "player_0_button_a"))
    }

    @Test
    fun `a target with several sources keeps the first one Eden can bind`() {
        val mapping = defaultSwitchProMapping(JoyconSide.DUAL) + mapOf("A" to "X|Y")

        val result = EdenDsuConfig.merge(null, listOf(pair(PlayerNumber.P1))) { mapping }

        assertEquals("\"${device(0)},button:4096\"", valueOf(result, "player_0_button_a"))
    }

    @Test
    fun `unrelated controls keys and other sections survive`() {
        val existing = "[Controls]\nvibration_enabled=true\n[Core]\nuse_multi_core=true"

        val result = merge(existing, listOf(solo(PlayerNumber.P1)))

        assertEquals("true", valueOf(result, "vibration_enabled"))
        assertTrue(result.contains("[Core]"))
        assertEquals("true", valueOf(result, "use_multi_core"))
    }
}
