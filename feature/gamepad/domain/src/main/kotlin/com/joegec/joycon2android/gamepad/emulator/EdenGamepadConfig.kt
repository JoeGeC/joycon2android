package com.joegec.joycon2android.gamepad.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.emittedFor
import com.joegec.joycon2android.buttonmapping.emittedStick
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.toSourceMap
import com.joegec.joycon2android.buttonmapping.toStickDirectionMap
import com.joegec.joycon2android.buttonmapping.wholeEmittedStick
import com.joegec.joycon2android.emulatorconfig.EdenControls
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.emulatorconfig.defineEdenKey
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState

/**
 * Generates Eden's `config.ini` `[Controls]` bindings for the Virtual Gamepad, driven by the
 * user's customizable Joy-Con -> Pro Controller mapping.
 *
 * The relay exposes every player as one standard Android HID gamepad, wired so each Joy-Con button
 * lands on the keycode of the same name (Switch A is BUTTON_A, ZL is BUTTON_L2, − is BUTTON_SELECT).
 * The d-pad is the HID hat (HAT_X = axis 15, HAT_Y = axis 16); [KEY_CODES]/[HAT_AXES] are that
 * fixed, body-independent wiring.
 *
 * Eden does not translate a sideways single Joy-Con: it only sets an `is_horizontal` flag (which on
 * hardware the game's own nn::hid honours, but Eden has no equivalent), and it masks an npad by
 * type — a JoyconLeft can't even report A/B/X/Y. So we present each single Joy-Con as a Pro
 * Controller and apply the sideways rotation ourselves: [inputFor] resolves a customized source to
 * what its body actually emits, so e.g. the left Joy-Con's d-pad resolves to the face-button
 * keycodes it is rotated onto.
 *
 * A target stick whose directions still follow one real stick binds its axes, keeping the analog
 * range; any other arrangement is assembled from its directions with [EdenControls.stickFromButtons].
 *
 * Each pad's [EdenGamepad] — port and guid both — comes from the app's read of the live
 * input-device list, since neither can be derived from the player number.
 */
object EdenGamepadConfig {
    // Joy-Con button -> the Android keycode the relay's HID gamepad emits for it. ReportMapper
    // places each one so the keycode carries its own name; Capture and GL take the two gamepad slots
    // with no Switch equivalent, and GR/Chat the trailing vendor collection's BUTTON_1/BUTTON_2.
    private const val A = 96
    private const val B = 97
    private const val CAPTURE = 98
    private const val X = 99
    private const val Y = 100
    private const val PADDLE_LEFT = 101
    private const val L = 102
    private const val R = 103
    private const val ZL = 104
    private const val ZR = 105
    private const val LS_CLICK = 106
    private const val RS_CLICK = 107
    private const val PLUS = 108
    private const val MINUS = 109
    private const val HOME = 110
    private const val PADDLE_RIGHT = 188
    private const val CHAT = 189

    private const val HAT_X = 15
    private const val HAT_Y = 16

    private val KEY_CODES = mapOf(
        JoyconButton.A to A, JoyconButton.B to B, JoyconButton.X to X, JoyconButton.Y to Y,
        JoyconButton.L to L, JoyconButton.R to R, JoyconButton.ZL to ZL, JoyconButton.ZR to ZR,
        JoyconButton.Minus to MINUS, JoyconButton.Plus to PLUS,
        JoyconButton.LS to LS_CLICK, JoyconButton.RS to RS_CLICK,
        JoyconButton.Home to HOME, JoyconButton.Capture to CAPTURE,
        JoyconButton.GL to PADDLE_LEFT, JoyconButton.GR to PADDLE_RIGHT,
        JoyconButton.Chat to CHAT,
    )

    private val HAT_AXES = mapOf(
        JoyconButton.Up to Axis(HAT_Y, '-'),
        JoyconButton.Down to Axis(HAT_Y, '+'),
        JoyconButton.Left to Axis(HAT_X, '-'),
        JoyconButton.Right to Axis(HAT_X, '+'),
    )

    private val PLAYER_KEY = Regex("""player_\d+_(?!motion).*""")

    fun merge(
        existing: String?,
        players: List<PlayerState>,
        gamepads: Map<Int, EdenGamepad>,
        mappingFor: (JoyconSide) -> Map<String, String>,
    ): String {
        // Drop every player's prior bindings first: a layout or port change leaves stale keys that
        // would otherwise linger and cross-fire onto another player's port.
        val cleared = IniEditor.removeKeys(existing, EdenControls.SECTION) { it.matches(PLAYER_KEY) }
        return IniEditor.setKeys(cleared, EdenControls.SECTION, controlKeys(players, gamepads, mappingFor), assign = "=")
    }

    private fun controlKeys(
        players: List<PlayerState>,
        gamepads: Map<Int, EdenGamepad>,
        mappingFor: (JoyconSide) -> Map<String, String>,
    ): Map<String, String> {
        val keys = LinkedHashMap<String, String>()
        players.forEach { player ->
            val index = player.player.index
            if (index !in 1..4) return@forEach
            val gamepad = gamepads[index] ?: return@forEach
            val type = EdenControls.npadType(player) ?: return@forEach
            val side = sideFor(player) ?: return@forEach
            val layout = layoutFor(side, mappingFor(side))
            val p = index - 1
            val device = "engine:android,port:${gamepad.port},guid:${gamepad.guid},pad:0"
            val display = "Joy-Con Virtual Gamepad $index ${gamepad.port}"

            keys.defineEdenKey("player_${p}_type", type.toString())
            keys.defineEdenKey("player_${p}_connected", "true")
            layout.buttons.forEach { (key, input) ->
                keys.defineEdenKey("player_${p}_$key", EdenControls.quote("$device,${input.spec()},display:$display"))
            }
            layout.sticks.forEach { (key, stick) ->
                val binding = when (stick) {
                    is AnalogStick -> "$device,axis_x:${stick.axes.first},axis_y:${stick.axes.second}," +
                        "offset_x:0,offset_y:0,invert_x:+,invert_y:-,display:$display"
                    is DigitalStick -> EdenControls.stickFromButtons(
                        stick.directions.mapValues { (_, input) -> "$device,${input.spec()},display:$display" },
                    )
                }
                keys.defineEdenKey("player_${p}_$key", EdenControls.quote(binding))
            }
        }
        return keys
    }

    private fun sideFor(player: PlayerState): JoyconSide? = when {
        player.hasPro || player.hasFullController -> JoyconSide.DUAL
        player.left != null && player.right == null -> JoyconSide.LEFT
        player.right != null && player.left == null -> JoyconSide.RIGHT
        else -> null
    }

    private fun layoutFor(side: JoyconSide, mapping: Map<String, String>): Layout {
        val buttons = mapping.toSourceMap<SwitchProButton>().mapNotNull { (target, sources) ->
            inputFor(side, sources)?.let { EdenControls.BUTTON_KEYS.getValue(target) to it }
        }.toMap()
        val sticks = mapping.toStickDirectionMap<SwitchProStick>().mapNotNull { (target, directions) ->
            stickFor(side, directions)?.let { EdenControls.STICK_KEYS.getValue(target) to it }
        }.toMap()
        return Layout(buttons, sticks)
    }

    private fun stickFor(side: JoyconSide, directions: Map<StickDirection, List<MappingSource>>): Stick? {
        directions.wholeEmittedStick(side)?.let { return AnalogStick(axesOf(it)) }
        val inputs = directions.mapNotNull { (direction, sources) -> inputFor(side, sources)?.let { direction to it } }
        return inputs.takeIf { it.isNotEmpty() }?.let { DigitalStick(it.toMap()) }
    }

    // Eden binds one input per key, so a target driven by several sources keeps the first that its
    // body can actually emit; the rest are only reachable through Dolphin.
    private fun inputFor(side: JoyconSide, sources: List<MappingSource>): Input? =
        sources.firstNotNullOfOrNull { inputFor(side, it) }

    private fun inputFor(side: JoyconSide, source: MappingSource): Input? = when (source) {
        is MappingSource.Button -> source.button.emittedFor(side)?.let { KEY_CODES[it]?.let(::Key) ?: HAT_AXES[it] }
        is MappingSource.Stick -> tiltOf(source.emittedStick(side), source.direction)
    }

    // Physical left stick lands on Android axes 0/1, physical right stick on axes 11/14 (see
    // ReportMapper); Android's Y axis grows downward.
    private fun axesOf(stick: StickSource) = if (stick == StickSource.LEFT_STICK) 0 to 1 else 11 to 14

    private fun tiltOf(stick: StickSource, direction: StickDirection): Axis {
        val (x, y) = axesOf(stick)
        return when (direction) {
            StickDirection.UP -> Axis(y, '-')
            StickDirection.DOWN -> Axis(y, '+')
            StickDirection.LEFT -> Axis(x, '-')
            StickDirection.RIGHT -> Axis(x, '+')
        }
    }

    private data class Layout(val buttons: Map<String, Input>, val sticks: Map<String, Stick>)

    private sealed interface Input {
        fun spec(): String
    }

    private data class Key(val code: Int) : Input {
        override fun spec() = "button:$code"
    }

    private data class Axis(val axis: Int, val invert: Char) : Input {
        override fun spec() = "axis:$axis,threshold:0.5,invert:$invert"
    }

    private sealed interface Stick

    private data class AnalogStick(val axes: Pair<Int, Int>) : Stick

    private data class DigitalStick(val directions: Map<StickDirection, Input>) : Stick
}
