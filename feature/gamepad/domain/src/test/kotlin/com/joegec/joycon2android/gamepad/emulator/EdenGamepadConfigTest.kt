package com.joegec.joycon2android.gamepad.emulator

import com.joegec.joycon2android.buttonmapping.DefaultControllerMappings
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// The default mapping, expressed as the opaque string map the repository would hand back —
// mirrors ObserveControllerMappingUseCase's defaultsFor without pulling in a DataStore dependency.
private fun defaultSwitchProMapping(side: JoyconSide): Map<String, String> =
    DefaultControllerMappings.switchProButtons(side).toNames() + DefaultControllerMappings.switchProSticks(side).toNames()

private fun <K : Enum<K>, V : Enum<V>> Map<K, V>.toNames(): Map<String, String> =
    entries.associate { it.key.name to it.value.name }

class EdenGamepadConfigTest {

    private fun joycon(side: Side) = ConnectedJoycon(address = side.name, side = side, deviceName = "Joy-Con")

    private fun merge(existing: String?, players: List<PlayerState>, ports: Map<Int, Int>) =
        EdenGamepadConfig.merge(
            existing,
            players,
            ports.mapValues { (_, port) -> EdenGamepad.of(port, VENDOR_ID, PRODUCT_ID) },
            ::defaultSwitchProMapping,
        )

    @Test
    fun `type reflects the layout and bindings use the resolved port`() {
        val players = listOf(
            PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)), // dual
            PlayerState(PlayerNumber.P2, left = joycon(Side.LEFT)),                              // left
            PlayerState(PlayerNumber.P3, right = joycon(Side.RIGHT)),                            // right
        )
        // P2/P3 enumerated out of player order, like a real device list
        val ports = mapOf(1 to 0, 2 to 2, 3 to 1)

        val result = merge(null, players, ports)

        assertTrue(result.contains("[Controls]"))
        assertTrue(result.contains("player_0_type=1")) // dual
        assertTrue(result.contains("player_1_type=0")) // left → Pro
        assertTrue(result.contains("player_2_type=0")) // right → Pro
        assertTrue(result.contains("player_0_button_a\\default=false"))
    }

    @Test
    fun `dual binds every button to the keycode of the same name`() {
        val players = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)))

        val result = merge(null, players, mapOf(1 to 0))

        assertTrue(result.contains("player_0_button_a=\"$DEVICE,button:96,display:Joy-Con Virtual Gamepad 1 0\""))   // BUTTON_A
        assertTrue(result.contains("player_0_button_x=\"$DEVICE,button:99,display:Joy-Con Virtual Gamepad 1 0\""))   // BUTTON_X
        assertTrue(result.contains("player_0_button_y=\"$DEVICE,button:100,display:Joy-Con Virtual Gamepad 1 0\""))  // BUTTON_Y
        assertTrue(result.contains("player_0_button_l=\"$DEVICE,button:102,display:Joy-Con Virtual Gamepad 1 0\""))  // BUTTON_L1
        assertTrue(result.contains("player_0_button_zr=\"$DEVICE,button:105,display:Joy-Con Virtual Gamepad 1 0\"")) // BUTTON_R2
        assertTrue(result.contains("player_0_button_minus=\"$DEVICE,button:109,display:Joy-Con Virtual Gamepad 1 0\"")) // BUTTON_SELECT
        assertTrue(result.contains("player_0_lstick=\"$DEVICE,axis_x:0,axis_y:1"))
        assertTrue(result.contains("player_0_rstick=\"$DEVICE,axis_x:11,axis_y:14"))
    }

    @Test
    fun `sideways right Joy-Con is a Pro Controller with faces rotated 90 degrees CW`() {
        val players = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = merge(null, players, mapOf(1 to 0))

        assertTrue(result.contains("player_0_type=0")) // Pro
        // The rotated cluster lands on the relay's own face buttons, so each face is a distinct key.
        assertTrue(result.contains("player_0_button_a=\"$DEVICE,button:96,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_b=\"$DEVICE,button:97,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_x=\"$DEVICE,button:99,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_y=\"$DEVICE,button:100,display:Joy-Con Virtual Gamepad 1 0\""))
        // SL/SR are the shoulders; on this body the relay reports them as L1/L2 (102/104).
        assertTrue(result.contains("player_0_button_l=\"$DEVICE,button:102,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_r=\"$DEVICE,button:104,display:Joy-Con Virtual Gamepad 1 0\""))
        // The lone stick is the main (left) stick so games read it for movement/steering.
        assertTrue(result.contains("player_0_lstick=\"$DEVICE,axis_x:0,axis_y:1"))
        assertFalse(result.contains("player_0_rstick="))
    }

    @Test
    fun `sideways left Joy-Con is a Pro Controller with its directions mapped to faces`() {
        val players = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT)))

        val result = merge(null, players, mapOf(1 to 0))

        assertTrue(result.contains("player_0_type=0")) // Pro
        // Directions become faces (90° CCW): A <- Down, B <- Left, X <- Right, Y <- Up.
        assertTrue(result.contains("player_0_button_a=\"$DEVICE,button:96,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_b=\"$DEVICE,button:97,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_x=\"$DEVICE,button:99,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_y=\"$DEVICE,button:100,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_lstick=\"$DEVICE,axis_x:0,axis_y:1"))
        // SL/SR are the shoulders; on this body the relay reports them as R1/R2 (103/105), since
        // the left Joy-Con's own L/ZL already hold the left pair.
        assertTrue(result.contains("player_0_button_l=\"$DEVICE,button:103,display:Joy-Con Virtual Gamepad 1 0\""))
        assertTrue(result.contains("player_0_button_r=\"$DEVICE,button:105,display:Joy-Con Virtual Gamepad 1 0\""))
        // A lone Joy-Con has no d-pad, and its own shoulders point away in this grip.
        assertFalse(result.contains("player_0_button_dup="))
        assertFalse(result.contains("player_0_button_zl="))
        assertFalse(result.contains("player_0_button_zr="))
    }

    @Test
    fun `guid follows the ids the platform reports, not the ids the relay was created with`() {
        val players = listOf(PlayerState(PlayerNumber.P1, left = joycon(Side.LEFT), right = joycon(Side.RIGHT)))
        // A handheld that re-publishes our pad under its built-in controller's vendor/product.
        val republished = mapOf(1 to EdenGamepad.of(port = 2, vendorId = 0x2020, productId = 0x0111))

        val result = EdenGamepadConfig.merge(null, players, republished, ::defaultSwitchProMapping)

        assertTrue(result.contains("guid:00000000000001110000000000002020"))
        assertFalse(result.contains(GUID))
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

        val result = merge(existing, players, mapOf(2 to 2))

        assertFalse(result.contains("port:1")) // stale port-1 face key gone
        assertTrue(result.contains("motion_enabled=true")) // unrelated key preserved
        // Rewritten cleanly onto the new port.
        assertTrue(result.contains("player_1_button_a=\"engine:android,port:2,guid:$GUID,pad:0,button:96,"))
    }

    @Test
    fun `players without a resolved port are skipped, others preserved`() {
        val existing = "[Controls]\nmotion_enabled=true\n[Cpu]\nfoo=bar\n"
        val players = listOf(PlayerState(PlayerNumber.P1, right = joycon(Side.RIGHT)))

        val result = merge(existing, players, emptyMap())

        // No port → no bindings written, but unrelated keys/sections stay
        assertTrue(result.contains("motion_enabled=true"))
        assertTrue(result.contains("[Cpu]"))
        assertTrue(result.contains("foo=bar"))
    }

    private companion object {
        // The ids UhidRelay creates our virtual gamepads with.
        const val VENDOR_ID = 0x1234
        const val PRODUCT_ID = 0x5678
        const val GUID = "00000000000056780000000000001234"
        const val DEVICE = "engine:android,port:0,guid:$GUID,pad:0"
    }
}
