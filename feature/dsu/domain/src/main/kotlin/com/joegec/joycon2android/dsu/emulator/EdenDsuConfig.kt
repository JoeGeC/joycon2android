package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.emittedFor
import com.joegec.joycon2android.buttonmapping.emittedStick
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.toSourceMap
import com.joegec.joycon2android.buttonmapping.toStickDirectionMap
import com.joegec.joycon2android.buttonmapping.wholeEmittedStick
import com.joegec.joycon2android.dsu.DsuConfig
import com.joegec.joycon2android.dsu.DsuSlots
import com.joegec.joycon2android.emulatorconfig.EdenControls
import com.joegec.joycon2android.emulatorconfig.EdenPaths
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.emulatorconfig.defineEdenKey
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState

/** How Eden addresses a cemuhook pad: docs/dsu-motion.md#edens-cemuhook-bindings */
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
        JoyconButton.Home to 0x40000, JoyconButton.Capture to 0x80000,
    )

    private val LEFT_STICK_AXES = 0 to 1
    private val RIGHT_STICK_AXES = 2 to 3

    fun path(packageName: String) = EdenPaths.config(packageName)

    fun merge(
        existing: String?,
        players: List<PlayerState>,
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): String {
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
        // Off, none of the bindings below resolve.
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
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): Map<String, String> {
        val secondHands = DsuSlots.secondHands(players).associate { it.state.player to it.slot }
        val keys = LinkedHashMap<String, String>()
        players.forEach { player ->
            val slot = player.player.index - 1
            if (slot !in 0 until DsuSlots.COUNT) return@forEach
            val type = EdenControls.npadType(player) ?: return@forEach
            val side = sideFor(player) ?: return@forEach
            val mapping = mappingFor(PlayerBody(player.player, side))
            val device = device(slot)

            keys.defineEdenKey("player_${slot}_type", type.toString())
            keys.defineEdenKey("player_${slot}_connected", "true")
            buttonBindings(side, mapping).forEach { (key, input) ->
                keys.defineEdenKey("player_${slot}_$key", EdenControls.quote("$device,$input"))
            }
            stickBindings(side, mapping, device).forEach { (key, binding) ->
                keys.defineEdenKey("player_${slot}_$key", EdenControls.quote(binding))
            }
            keys.defineEdenKey("player_${slot}_motionright", motion(slot))
            keys.defineEdenKey("player_${slot}_motionleft", motion(secondHands[player.player] ?: slot))
        }
        return keys
    }

    private fun device(pad: Int) = "engine:$ENGINE,guid:$LOOPBACK_GUID,port:${DsuConfig.PORT},pad:$pad"

    private fun motion(pad: Int) = EdenControls.quote("${device(pad)},motion:0")

    private fun buttonBindings(side: JoyconSide, mapping: Map<String, String>): Map<String, String> =
        mapping.toSourceMap<SwitchProButton>().mapNotNull { (target, sources) ->
            inputFor(side, sources)?.let { EdenControls.BUTTON_KEYS.getValue(target) to it }
        }.toMap()

    private fun stickBindings(side: JoyconSide, mapping: Map<String, String>, device: String): Map<String, String> =
        mapping.toStickDirectionMap<SwitchProStick>().mapNotNull { (target, directions) ->
            stickFor(side, directions, device)?.let { EdenControls.STICK_KEYS.getValue(target) to it }
        }.toMap()

    private fun stickFor(side: JoyconSide, directions: Map<StickDirection, List<MappingSource>>, device: String): String? {
        directions.wholeEmittedStick(side)?.let { stick ->
            val (x, y) = axesOf(stick)
            return "$device,axis_x:$x,axis_y:$y"
        }
        val inputs = directions.mapNotNull { (direction, sources) ->
            inputFor(side, sources)?.let { direction to "$device,$it" }
        }
        return inputs.takeIf { it.isNotEmpty() }?.let { EdenControls.stickFromButtons(it.toMap()) }
    }

    private fun axesOf(stick: StickSource) =
        if (stick == StickSource.LEFT_STICK) LEFT_STICK_AXES else RIGHT_STICK_AXES

    private fun inputFor(side: JoyconSide, sources: List<MappingSource>): String? =
        sources.firstNotNullOfOrNull { inputFor(side, it) }

    private fun inputFor(side: JoyconSide, source: MappingSource): String? = when (source) {
        is MappingSource.Button -> source.button.emittedFor(side)?.let(DS4_BITS::get)?.let { "button:$it" }
        is MappingSource.Stick -> tiltOf(source.emittedStick(side), source.direction)
    }

    // Eden reads a cemuhook stick byte as (v - 127) / 127, so up and right are the positive ends.
    private fun tiltOf(stick: StickSource, direction: StickDirection): String {
        val (x, y) = axesOf(stick)
        val (axis, invert) = when (direction) {
            StickDirection.UP -> y to '+'
            StickDirection.DOWN -> y to '-'
            StickDirection.LEFT -> x to '-'
            StickDirection.RIGHT -> x to '+'
        }
        return "axis:$axis,threshold:0.5,invert:$invert"
    }

    private fun sideFor(player: PlayerState): JoyconSide? = when {
        player.hasPro || player.hasFullController -> JoyconSide.DUAL
        player.left != null -> JoyconSide.LEFT
        player.right != null -> JoyconSide.RIGHT
        else -> null
    }
}
