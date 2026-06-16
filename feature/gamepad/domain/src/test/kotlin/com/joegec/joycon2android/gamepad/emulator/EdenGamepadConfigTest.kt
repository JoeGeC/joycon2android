package com.joegec.joycon2android.gamepad.emulator

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EdenGamepadConfigTest {

    private fun joycon(side: Side) = ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con")

    @Test
    fun `type reflects the layout and bindings use the resolved port`() {
        val players = listOf(
            PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)), // dual
            PlayerState(PlayerNumber.P2, left = joycon(Side.LEFT)),                              // left
            PlayerState(PlayerNumber.P3, right = joycon(Side.RIGHT)),                            // right
        )
        // P2/P3 enumerated out of player order, like a real device list
        val ports = mapOf(1 to 0, 2 to 2, 3 to 1)

        val result = EdenGamepadConfig.merge(null, players, ports)

        assertTrue(result.contains("[Controls]"))
        assertTrue(result.contains("player_0_type=1")) // dual
        assertTrue(result.contains("player_1_type=0")) // left → Pro
        assertTrue(result.contains("player_2_type=0")) // right → Pro
        assertTrue(result.contains("player_0_button_a\\default=false"))
    }

    @Test
    fun `dual uses native keycodes accounting for the HID button shift`() {
        val players = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)))

        val result = EdenGamepadConfig.merge(null, players, ports = mapOf(1 to 0))

        // Switch X is BTN_C (98), Switch Y is BTN_X (99) — not 99/100.
        assertTrue(result.contains("player_0_button_a=\"engine:android,port:0,guid:$GUID,button:96,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_x=\"engine:android,port:0,guid:$GUID,button:98,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_y=\"engine:android,port:0,guid:$GUID,button:99,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("button:100")) // L
        assertTrue(result.contains("player_0_lstick=\"engine:android,port:0,guid:$GUID,axis_x:0,axis_y:1"))
        assertTrue(result.contains("player_0_rstick=\"engine:android,port:0,guid:$GUID,axis_x:11,axis_y:14"))
    }

    @Test
    fun `sideways right Joy-Con is a Pro Controller with faces rotated 90 degrees CW`() {
        val players = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = EdenGamepadConfig.merge(null, players, ports = mapOf(1 to 0))

        assertTrue(result.contains("player_0_type=0")) // Pro
        // A <- physical X (98), B <- physical A (96), X <- physical Y (99), Y <- physical B (97)
        assertTrue(result.contains("player_0_button_a=\"engine:android,port:0,guid:$GUID,button:98,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_b=\"engine:android,port:0,guid:$GUID,button:96,display:Joy-Con Virtual Gamepad 1 0\""))
        // SL/SR (relay-remapped to 100/102) become the L/R shoulders.
        assertTrue(result.contains("player_0_button_l=\"engine:android,port:0,guid:$GUID,button:100,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_r=\"engine:android,port:0,guid:$GUID,button:102,display:Joy-Con Virtual Gamepad 1 0\""))
        // The lone stick is the main (left) stick so games read it for movement/steering.
        assertTrue(result.contains("player_0_lstick=\"engine:android,port:0,guid:$GUID,axis_x:0,axis_y:1"))
        assertFalse(result.contains("player_0_rstick="))
    }

    @Test
    fun `sideways left Joy-Con is a Pro Controller with its directions mapped to faces`() {
        val players = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT)))

        val result = EdenGamepadConfig.merge(null, players, ports = mapOf(1 to 0))

        assertTrue(result.contains("player_0_type=0")) // Pro
        // Directions become faces (90° CCW): A <- Down (hat_x+), Y <- Up (hat_x-).
        assertTrue(result.contains("player_0_button_a=\"engine:android,port:0,guid:$GUID,axis:15,threshold:0.5,invert:+,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_y=\"engine:android,port:0,guid:$GUID,axis:15,threshold:0.5,invert:-,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_x=\"engine:android,port:0,guid:$GUID,axis:16,threshold:0.5,invert:-,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_lstick=\"engine:android,port:0,guid:$GUID,axis_x:0,axis_y:1"))
        // The four directions are consumed by the faces, so no d-pad is bound.
        assertFalse(result.contains("player_0_button_dup="))
    }

    @Test
    fun `a prior layout's stale keys are cleared so they cannot cross-fire`() {
        // P2 was previously a full controller on port 1; now it's a left Joy-Con on port 2.
        val existing = """
            [Controls]
            player_1_button_a="engine:android,port:1,guid:$GUID,button:96,display:old"
            player_1_button_a\default=false
            motion_enabled=true
        """.trimIndent()
        val players = listOf(PlayerState(PlayerNumber.P2, left = joycon(Side.LEFT)))

        val result = EdenGamepadConfig.merge(existing, players, ports = mapOf(2 to 2))

        assertFalse(result.contains("button:96")) // stale port-1 face key gone
        assertTrue(result.contains("motion_enabled=true")) // unrelated key preserved
        // Rewritten cleanly: face A is now the axis-derived binding on the new port 2.
        assertTrue(result.contains("player_1_button_a=\"engine:android,port:2,guid:$GUID,axis:15,threshold:0.5,invert:+,"))
    }

    @Test
    fun `players without a resolved port are skipped, others preserved`() {
        val existing = "[Controls]\nmotion_enabled=true\n[Cpu]\nfoo=bar\n"
        val players = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = EdenGamepadConfig.merge(existing, players, ports = emptyMap())

        // No port → no bindings written, but unrelated keys/sections stay
        assertTrue(result.contains("motion_enabled=true"))
        assertTrue(result.contains("[Cpu]"))
        assertTrue(result.contains("foo=bar"))
    }

    private companion object {
        const val GUID = "00000000000056780000000000001234"
    }
}
