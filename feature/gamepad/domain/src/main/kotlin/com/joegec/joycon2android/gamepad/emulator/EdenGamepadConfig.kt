package com.joegec.joycon2android.gamepad.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.toButtonMap
import com.joegec.joycon2android.buttonmapping.toStickMap
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.SidewaysMapper

/**
 * Generates Eden's `config.ini` `[Controls]` bindings for the Virtual Gamepad, driven by the
 * user's customizable Joy-Con -> Pro Controller mapping.
 *
 * The relay exposes every player as one standard Android HID gamepad. Its 14 buttons land on
 * Android keycodes in the fixed HID gamepad order — BTN_A, BTN_B, BTN_C, BTN_X, BTN_Y, BTN_Z,
 * BTN_TL… — so a Joy-Con button maps to a *shifted* keycode: Switch X is BTN_C (98), Switch Y is
 * BTN_X (99), L is BTN_Y (100), and so on. The d-pad is the HID hat (HAT_X = axis 15, HAT_Y =
 * axis 16); [KEY_CODES]/[HAT_AXES] are that fixed, body-independent wiring.
 *
 * Eden does not translate a sideways single Joy-Con: it only sets an `is_horizontal` flag (which on
 * hardware the game's own nn::hid honours, but Eden has no equivalent), and it masks an npad by
 * type — a JoyconLeft can't even report A/B/X/Y. So we present each single Joy-Con as a Pro
 * Controller and apply the sideways rotation ourselves: [inputFor] runs a customized source
 * through the same [SidewaysMapper] remap used for live HID output before resolving its keycode,
 * so e.g. the left Joy-Con's d-pad still lands on the hat that becomes the face buttons.
 *
 * Our pads share one `guid` (VID 0x1234 / PID 0x5678); Eden distinguishes them by `port`, the
 * device's enumeration rank, supplied by the app from the live input-device list.
 */
object EdenGamepadConfig {
    const val PACKAGE = "dev.eden.eden_emulator"
    val path = "/sdcard/Android/data/$PACKAGE/files/config/config.ini"

    private const val GUID = "00000000000056780000000000001234"

    // Joy-Con button -> Android keycode the relay's HID gamepad emits (note the BTN_A..BTN_Z shift).
    private const val A = 96
    private const val B = 97
    private const val X = 98
    private const val Y = 99
    private const val L = 100
    private const val R = 101
    private const val ZL = 102
    private const val ZR = 103
    private const val MINUS = 104
    private const val PLUS = 105
    private const val CAPTURE = 106
    private const val RS_CLICK = 108
    private const val LS_CLICK = 109
    private const val HOME = 110
    private const val HAT_X = 15
    private const val HAT_Y = 16

    private val KEY_CODES = mapOf(
        JoyconButton.A to A, JoyconButton.B to B, JoyconButton.X to X, JoyconButton.Y to Y,
        JoyconButton.L to L, JoyconButton.R to R, JoyconButton.ZL to ZL, JoyconButton.ZR to ZR,
        JoyconButton.Minus to MINUS, JoyconButton.Plus to PLUS,
        JoyconButton.LS to LS_CLICK, JoyconButton.RS to RS_CLICK,
        JoyconButton.Home to HOME, JoyconButton.Camera to CAPTURE,
    )

    private val HAT_AXES = mapOf(
        JoyconButton.Up to Axis(HAT_Y, '-'),
        JoyconButton.Down to Axis(HAT_Y, '+'),
        JoyconButton.Left to Axis(HAT_X, '-'),
        JoyconButton.Right to Axis(HAT_X, '+'),
    )

    private val EDEN_KEYS = mapOf(
        SwitchProButton.A to "button_a", SwitchProButton.B to "button_b",
        SwitchProButton.X to "button_x", SwitchProButton.Y to "button_y",
        SwitchProButton.L to "button_l", SwitchProButton.R to "button_r",
        SwitchProButton.ZL to "button_zl", SwitchProButton.ZR to "button_zr",
        SwitchProButton.Plus to "button_plus", SwitchProButton.Minus to "button_minus",
        SwitchProButton.Home to "button_home", SwitchProButton.Capture to "button_screenshot",
        SwitchProButton.LStickClick to "button_lstick", SwitchProButton.RStickClick to "button_rstick",
        SwitchProButton.DPadUp to "button_dup", SwitchProButton.DPadDown to "button_ddown",
        SwitchProButton.DPadLeft to "button_dleft", SwitchProButton.DPadRight to "button_dright",
    )

    private val STICK_KEYS = mapOf(SwitchProStick.LStick to "lstick", SwitchProStick.RStick to "rstick")

    private val PLAYER_KEY = Regex("""player_\d+_.*""")

    fun merge(
        existing: String?,
        players: List<PlayerState>,
        ports: Map<Int, Int>,
        mappingFor: (JoyconSide) -> Map<String, String>,
    ): String {
        // Drop every player's prior bindings first: a layout or port change leaves stale keys that
        // would otherwise linger and, sharing our single guid, cross-fire onto another player's port.
        val cleared = IniEditor.removeKeys(existing, "[Controls]") { it.matches(PLAYER_KEY) }
        return IniEditor.setKeys(cleared, "[Controls]", controlKeys(players, ports, mappingFor), assign = "=")
    }

    private fun controlKeys(
        players: List<PlayerState>,
        ports: Map<Int, Int>,
        mappingFor: (JoyconSide) -> Map<String, String>,
    ): Map<String, String> {
        val keys = LinkedHashMap<String, String>()
        players.forEach { player ->
            val index = player.player.index
            if (index !in 1..4) return@forEach
            val port = ports[index] ?: return@forEach
            val type = typeFor(player) ?: return@forEach
            val side = sideFor(player) ?: return@forEach
            val layout = layoutFor(side, mappingFor(side))
            val p = index - 1
            val device = "engine:android,port:$port,guid:$GUID"
            val display = "Joy-Con Virtual Gamepad $index $port"

            keys.define("player_${p}_type", type.toString())
            keys.define("player_${p}_connected", "true")
            layout.buttons.forEach { (key, input) ->
                keys.define("player_${p}_$key", quote("$device,${input.spec()},display:$display"))
            }
            layout.sticks.forEach { (key, axes) ->
                val stick = "axis_x:${axes.first},axis_y:${axes.second},offset_x:0,offset_y:0,invert_x:+,invert_y:-"
                keys.define("player_${p}_$key", quote("$device,$stick,display:$display"))
            }
        }
        return keys
    }

    // Eden falls back to the engine default when a binding's `\default` flag is true, so pin both.
    private fun MutableMap<String, String>.define(key: String, value: String) {
        this[key] = value
        this["$key\\default"] = "false"
    }

    private fun quote(value: String) = "\"$value\""

    private fun typeFor(player: PlayerState): Int? = when {
        player.hasPro -> 0                                          // Pro Controller
        player.hasFullController -> 1                               // Dual Joy-Con
        // Both single Joy-Cons report as Pro: Eden doesn't translate a sideways Joy-Con's stick or
        // shoulders, so we present a normalised full controller and do the rotation ourselves.
        player.left != null && player.right == null -> 0
        player.right != null && player.left == null -> 0
        else -> null
    }

    private fun sideFor(player: PlayerState): JoyconSide? = when {
        player.hasPro || player.hasFullController -> JoyconSide.DUAL
        player.left != null && player.right == null -> JoyconSide.LEFT
        player.right != null && player.left == null -> JoyconSide.RIGHT
        else -> null
    }

    private fun layoutFor(side: JoyconSide, mapping: Map<String, String>): Layout {
        val buttons = mapping.toButtonMap<SwitchProButton>().mapNotNull { (target, source) ->
            inputFor(side, source)?.let { EDEN_KEYS.getValue(target) to it }
        }.toMap()
        val sticks = if (side == JoyconSide.DUAL) {
            mapping.toStickMap<SwitchProStick>().entries.associate { (target, source) ->
                STICK_KEYS.getValue(target) to axesFor(source)
            }
        } else {
            mapOf("lstick" to (0 to 1)) // the lone stick isn't user-routable — there's only one
        }
        return Layout(buttons, sticks)
    }

    private fun axesFor(source: StickSource) = if (source == StickSource.LEFT_STICK) 0 to 1 else 11 to 14

    // Applies the same physical -> virtual remap SidewaysMapper uses for live HID output, so a
    // customized source resolves to the keycode Eden would actually see for that body.
    private fun inputFor(side: JoyconSide, physical: JoyconButton): Input? {
        val virtualId = when (side) {
            JoyconSide.DUAL -> physical.id
            JoyconSide.LEFT -> SidewaysMapper.remapButtonsLeft(setOf(physical.id)).first()
            JoyconSide.RIGHT -> SidewaysMapper.remapButtonsRight(setOf(physical.id)).first()
        }
        val virtual = JoyconButton.entries.firstOrNull { it.id == virtualId } ?: return null
        return KEY_CODES[virtual]?.let { Key(it) } ?: HAT_AXES[virtual]
    }

    private data class Layout(val buttons: Map<String, Input>, val sticks: Map<String, Pair<Int, Int>>)

    private sealed interface Input {
        fun spec(): String
    }

    private data class Key(val code: Int) : Input {
        override fun spec() = "button:$code"
    }

    private data class Axis(val axis: Int, val invert: Char) : Input {
        override fun spec() = "axis:$axis,threshold:0.5,invert:$invert"
    }
}
