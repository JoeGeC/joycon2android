package com.joegec.joycon2android.gamepad.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.emittedFor
import com.joegec.joycon2android.buttonmapping.emittedStick
import com.joegec.joycon2android.buttonmapping.joyconSide
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.target.SwitchProTargets
import com.joegec.joycon2android.buttonmapping.toSourceMap
import com.joegec.joycon2android.buttonmapping.toStickDirectionMap
import com.joegec.joycon2android.buttonmapping.wholeEmittedStick
import com.joegec.joycon2android.emulatorconfig.EdenControls
import com.joegec.joycon2android.emulatorconfig.IniEditor
import com.joegec.joycon2android.emulatorconfig.defineEdenKey
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState

/** A lone Joy-Con's two grips: docs/virtual-gamepad.md#single-joy-con */
object EdenGamepadConfig {
    // Keycodes the relay emits: docs/virtual-gamepad.md#buttons-and-keycodes
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
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): String {
        val cleared = IniEditor.removeKeys(existing, EdenControls.SECTION) { it.matches(PLAYER_KEY) }
        return IniEditor.setKeys(cleared, EdenControls.SECTION, controlKeys(players, gamepads, mappingFor), assign = "=")
    }

    private fun controlKeys(
        players: List<PlayerState>,
        gamepads: Map<Int, EdenGamepad>,
        mappingFor: (PlayerBody) -> Map<String, String>,
    ): Map<String, String> {
        val keys = LinkedHashMap<String, String>()
        players.forEach { player ->
            val index = player.player.index
            if (index !in 1..4) return@forEach
            val gamepad = gamepads[index] ?: return@forEach
            val type = EdenControls.npadType(player) ?: return@forEach
            val side = player.joyconSide() ?: return@forEach
            val layout = layoutFor(side, player.isSingleJoycon, mappingFor(PlayerBody(player.player, side)))
            val p = index - 1
            val device = "engine:android,port:${gamepad.port},guid:${gamepad.guid},pad:0"
            val display = "Joy-Con Virtual Gamepad $index ${gamepad.port}"

            keys.defineEdenKey("player_${p}_type", type.toString())
            keys.defineEdenKey("player_${p}_connected", "true")
            // Eden's default for player 1 rumbles the phone instead of the pad.
            keys.defineEdenKey("player_${p}_use_system_vibrator", "false")
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

    private fun layoutFor(side: JoyconSide, single: Boolean, mapping: Map<String, String>): Layout {
        val buttonTargets = SwitchProTargets.buttons(side, single)
        val buttons = mapping.toSourceMap<SwitchProButton>().filterKeys { it in buttonTargets }
            .mapNotNull { (target, sources) ->
                inputFor(side, single, sources)?.let { EdenControls.buttonKey(target, side) to it }
            }.toMap()
        val stickTargets = SwitchProTargets.sticks(side, single)
        val sticks = mapping.toStickDirectionMap<SwitchProStick>().filterKeys { it in stickTargets }
            .mapNotNull { (target, directions) ->
                stickFor(side, single, directions)?.let { EdenControls.STICK_KEYS.getValue(target) to it }
            }.toMap()
        return Layout(buttons, sticks)
    }

    private fun stickFor(side: JoyconSide, single: Boolean, directions: Map<StickDirection, List<MappingSource>>): Stick? {
        directions.wholeEmittedStick(side, single)?.let { return AnalogStick(axesOf(it)) }
        val inputs = directions.mapNotNull { (direction, sources) ->
            inputFor(side, single, sources)?.let { direction to it }
        }
        return inputs.takeIf { it.isNotEmpty() }?.let { DigitalStick(it.toMap()) }
    }

    private fun inputFor(side: JoyconSide, single: Boolean, sources: List<MappingSource>): Input? =
        sources.firstNotNullOfOrNull { inputFor(side, single, it) }

    private fun inputFor(side: JoyconSide, single: Boolean, source: MappingSource): Input? = when (source) {
        is MappingSource.Button -> source.button.emittedFor(side, single)?.let(::inputOf)
        is MappingSource.Stick -> tiltOf(source.emittedStick(side, single), source.direction)
    }

    private fun inputOf(button: JoyconButton): Input? = KEY_CODES[button]?.let(::Key) ?: HAT_AXES[button]

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
