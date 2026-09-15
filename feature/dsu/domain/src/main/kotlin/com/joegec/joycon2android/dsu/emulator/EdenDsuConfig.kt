package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.toButtonMap
import com.joegec.joycon2android.buttonmapping.toStickMap
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.dsu.DsuSlots
import com.joegec.joycon2android.emulatorconfig.EdenControls
import com.joegec.joycon2android.emulatorconfig.EdenPaths
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.emulatorconfig.defineEdenKey
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.SidewaysMapper

/**
 * Binds Eden to our DSU server in `config.ini`'s `[Controls]`: the three server switches, then a
 * whole controller per assigned player — buttons, sticks and motion — driven by the user's
 * customizable Joy-Con -> Pro Controller mapping. A cemuhook pad carries the full DS4 button set
 * and both sticks, so nothing else is needed for a player to play; the Virtual Gamepad is an
 * alternative route to the same keys, not a prerequisite.
 *
 * Eden's cemuhook engine addresses a pad by `guid`, `port` and `pad`, and nothing else. The
 * server's `guid` is its IPv4 address as a 32-bit integer, hex, right-aligned in an otherwise-zero
 * UUID written raw (no dashes); `port` is the UDP port, not a controller index. `pad` is a global
 * index, `client * 4 + slot`, so with our server as Eden's only client it is the DSU slot itself.
 *
 * [DS4_BITS] is the protocol wiring, not a preference: cemuhook's two button bytes packed
 * low-then-high are exactly Eden's `PadButton` values, and Home and the touchpad click ride the
 * bytes above them. Sticks arrive as raw bytes that Eden reads as `(v - 127) / 127`, so the
 * axis pairs need no inversion.
 *
 * Motion is the one thing a pad cannot share: a pad packet carries a single accelerometer and
 * gyroscope, so `motion` is always index 0 and each hand streams on a slot of its own
 * ([DsuSlots]). The hand holding the player's own slot lands on `motionright`, its second hand on
 * `motionleft`; one Joy-Con, or a pair that ran out of slots, binds both to the same pad, which is
 * what Eden's own auto-mapping does for every device.
 */
object EdenDsuConfig {
    private const val ENGINE = "cemuhookudp"

    // 127.0.0.1 -> 0x7f000001, the low four bytes of an all-zero UUID.
    private const val LOOPBACK_GUID = "0000000000000000000000007f000001"

    private const val SERVER = "127.0.0.1:${DsuConfig.PORT}"

    private val DS4_BITS = mapOf(
        JoyconButton.Minus to 0x00001, JoyconButton.LS to 0x00002,
        JoyconButton.RS to 0x00004, JoyconButton.Plus to 0x00008,
        JoyconButton.Up to 0x00010, JoyconButton.Right to 0x00020,
        JoyconButton.Down to 0x00040, JoyconButton.Left to 0x00080,
        JoyconButton.ZL to 0x00100, JoyconButton.ZR to 0x00200,
        JoyconButton.L to 0x00400, JoyconButton.R to 0x00800,
        JoyconButton.X to 0x01000, JoyconButton.A to 0x02000,
        JoyconButton.B to 0x04000, JoyconButton.Y to 0x08000,
        JoyconButton.Home to 0x40000, JoyconButton.Camera to 0x80000,
    )

    private val LEFT_STICK_AXES = 0 to 1
    private val RIGHT_STICK_AXES = 2 to 3

    fun path(packageName: String) = EdenPaths.config(packageName)

    fun merge(
        existing: String?,
        players: List<PlayerState>,
        mappingFor: (JoyconSide) -> Map<String, String>,
    ): String {
        // A reassignment leaves stale bindings on players who no longer hold a controller, and
        // those would keep feeding an emulated pad from whoever now owns that slot.
        val cleared = IniEditor.removeKeys(existing, EdenControls.SECTION) { it.matches(EdenControls.PLAYER_KEY) }
        return IniEditor.setKeys(
            cleared,
            EdenControls.SECTION,
            serverKeys(existing) + playerKeys(players, mappingFor),
            assign = "=",
        )
    }

    private fun serverKeys(existing: String?): Map<String, String> {
        val keys = LinkedHashMap<String, String>()
        keys.defineEdenKey("motion_enabled", "true")
        keys.defineEdenKey("udp_input_servers", serverList(existing))
        // What makes Eden offer the UDP pads at all — off, and none of the bindings below resolve.
        keys.defineEdenKey("enable_udp_controller", "true")
        return keys
    }

    // Eden splits this setting on commas and dials every entry, so an existing server stays.
    private fun serverList(existing: String?): String {
        val configured = IniEditor.valueOf(existing, EdenControls.SECTION, "udp_input_servers")
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()
        return if (SERVER in configured) configured.joinToString(",") else (configured + SERVER).joinToString(",")
    }

    private fun playerKeys(
        players: List<PlayerState>,
        mappingFor: (JoyconSide) -> Map<String, String>,
    ): Map<String, String> {
        val secondHands = DsuSlots.secondHands(players).associate { it.state.player to it.slot }
        val keys = LinkedHashMap<String, String>()
        players.forEach { player ->
            val slot = player.player.index - 1
            if (slot !in 0 until DsuSlots.COUNT) return@forEach
            val type = EdenControls.npadType(player) ?: return@forEach
            val side = sideFor(player) ?: return@forEach
            val mapping = mappingFor(side)
            val device = device(slot)

            keys.defineEdenKey("player_${slot}_type", type.toString())
            keys.defineEdenKey("player_${slot}_connected", "true")
            buttonBindings(side, mapping).forEach { (key, bit) ->
                keys.defineEdenKey("player_${slot}_$key", EdenControls.quote("$device,button:$bit"))
            }
            stickBindings(side, mapping).forEach { (key, axes) ->
                keys.defineEdenKey(
                    "player_${slot}_$key",
                    EdenControls.quote("$device,axis_x:${axes.first},axis_y:${axes.second}"),
                )
            }
            keys.defineEdenKey("player_${slot}_motionright", motion(slot))
            keys.defineEdenKey("player_${slot}_motionleft", motion(secondHands[player.player] ?: slot))
        }
        return keys
    }

    private fun device(pad: Int) = "engine:$ENGINE,guid:$LOOPBACK_GUID,port:${DsuConfig.PORT},pad:$pad"

    private fun motion(pad: Int) = EdenControls.quote("${device(pad)},motion:0")

    private fun buttonBindings(side: JoyconSide, mapping: Map<String, String>): Map<String, Int> =
        mapping.toButtonMap<SwitchProButton>().mapNotNull { (target, source) ->
            bitFor(side, source)?.let { EdenControls.BUTTON_KEYS.getValue(target) to it }
        }.toMap()

    private fun stickBindings(side: JoyconSide, mapping: Map<String, String>): Map<String, Pair<Int, Int>> =
        if (side == JoyconSide.DUAL) {
            mapping.toStickMap<SwitchProStick>().entries.associate { (target, source) ->
                EdenControls.STICK_KEYS.getValue(target) to axesFor(source)
            }
        } else {
            // A sideways Joy-Con's lone stick isn't user-routable, and SidewaysMapper has already
            // rotated it onto the left-stick axes by the time it reaches the wire.
            mapOf("lstick" to LEFT_STICK_AXES)
        }

    private fun axesFor(source: StickSource) =
        if (source == StickSource.LEFT_STICK) LEFT_STICK_AXES else RIGHT_STICK_AXES

    private fun sideFor(player: PlayerState): JoyconSide? = when {
        player.hasPro || player.hasFullController -> JoyconSide.DUAL
        player.left != null -> JoyconSide.LEFT
        player.right != null -> JoyconSide.RIGHT
        else -> null
    }

    // Applies the same physical -> virtual remap SidewaysMapper uses for live output, so a
    // customized source resolves to the bit the DSU pad would actually set for that body.
    private fun bitFor(side: JoyconSide, physical: JoyconButton): Int? {
        val virtualId = when (side) {
            JoyconSide.DUAL -> physical.id
            JoyconSide.LEFT -> SidewaysMapper.remapButtonsLeft(setOf(physical.id)).first()
            JoyconSide.RIGHT -> SidewaysMapper.remapButtonsRight(setOf(physical.id)).first()
        }
        val virtual = JoyconButton.entries.firstOrNull { it.id == virtualId } ?: return null
        return DS4_BITS[virtual]
    }
}
