package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerState

/**
 * Generates Eden's `config.ini` `[Controls]` bindings for the Virtual Gamepad.
 *
 * The relay exposes every player as one standard Android HID gamepad. Its 14 buttons land on
 * Android keycodes in the fixed HID gamepad order — BTN_A, BTN_B, BTN_C, BTN_X, BTN_Y, BTN_Z,
 * BTN_TL… — so a Joy-Con button maps to a *shifted* keycode: Switch X is BTN_C (98), Switch Y is
 * BTN_X (99), L is BTN_Y (100), and so on. The d-pad is the HID hat (HAT_X = axis 15, HAT_Y =
 * axis 16); the lone stick of a sideways single Joy-Con arrives on the left-stick axes (0/1),
 * already rotated upright by the relay.
 *
 * Eden does not translate a sideways single Joy-Con: it only sets an `is_horizontal` flag (which on
 * hardware the game's own nn::hid honours, but Eden has no equivalent), and it masks an npad by
 * type — a JoyconLeft can't even report A/B/X/Y. So a sideways Joy-Con's stick can't land on the
 * main stick and its directions/SL-SR can't become faces/L-R under a single-Joy-Con type. We
 * therefore present each single Joy-Con as a Pro Controller and apply the sideways rotation here:
 * the lone stick becomes the left stick, SL/SR become L/R, and the four action buttons become
 * A/B/X/Y (the left Joy-Con's d-pad rotated 90° CCW, the right Joy-Con's diamond rotated 90° CW).
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

    private val DPAD = mapOf(
        "button_dup" to Axis(HAT_Y, '-'),
        "button_ddown" to Axis(HAT_Y, '+'),
        "button_dleft" to Axis(HAT_X, '-'),
        "button_dright" to Axis(HAT_X, '+'),
    )

    // Pro / dual: full button set, both sticks. Matches what Eden detects for our pad.
    private val FULL = Layout(
        buttons = DPAD + mapOf(
            "button_a" to Key(A), "button_b" to Key(B), "button_x" to Key(X), "button_y" to Key(Y),
            "button_l" to Key(L), "button_r" to Key(R), "button_zl" to Key(ZL), "button_zr" to Key(ZR),
            "button_minus" to Key(MINUS), "button_plus" to Key(PLUS),
            "button_lstick" to Key(LS_CLICK), "button_rstick" to Key(RS_CLICK),
            "button_home" to Key(HOME), "button_screenshot" to Key(CAPTURE),
        ),
        sticks = mapOf("lstick" to (0 to 1), "rstick" to (11 to 14)),
    )

    // Sideways right Joy-Con, presented as a Pro Controller for the same reason as the left: Eden
    // won't translate the sideways orientation, and games read the main (left) stick and L/R
    // shoulders — not the Joy-Con's native right stick / SL-SR — so we do that mapping. Faces rotate
    // 90° CW (A <- physical X, B <- physical A, X <- physical Y, Y <- physical B); the lone stick
    // becomes the left stick; SL/SR become L/R.
    private val RIGHT_AS_PRO = Layout(
        buttons = mapOf(
            "button_a" to Key(X), "button_b" to Key(A), "button_x" to Key(Y), "button_y" to Key(B),
            "button_l" to Key(L), "button_r" to Key(ZL),     // SL, SR (relay-remapped) → L/R shoulders
            "button_zl" to Key(R), "button_zr" to Key(ZR),   // native R / ZR
            "button_plus" to Key(PLUS), "button_home" to Key(HOME),
            "button_lstick" to Key(LS_CLICK),
        ),
        sticks = mapOf("lstick" to (0 to 1)),
    )

    // Sideways left Joy-Con, presented as a Pro Controller. A JoyconLeft npad has no A/B/X/Y, and
    // Eden never rotates its d-pad into faces — it only sets is_horizontal, which on hardware the
    // game's own nn::hid honours but Eden has no equivalent for. So we do the rotation the game
    // would: the four directions become A/B/X/Y. A <- Down, B <- Left, X <- Right, Y <- Up (90° CCW).
    private val LEFT_AS_PRO = Layout(
        buttons = mapOf(
            "button_a" to Axis(HAT_X, '+'), "button_b" to Axis(HAT_Y, '+'),
            "button_x" to Axis(HAT_Y, '-'), "button_y" to Axis(HAT_X, '-'),
            "button_l" to Key(R), "button_r" to Key(ZR),     // SL, SR (relay-remapped) → L/R shoulders
            "button_zl" to Key(L), "button_zr" to Key(ZL),   // native L / ZL
            "button_minus" to Key(MINUS),
            "button_lstick" to Key(LS_CLICK), "button_screenshot" to Key(CAPTURE),
        ),
        sticks = mapOf("lstick" to (0 to 1)),
    )

    private val PLAYER_KEY = Regex("""player_\d+_.*""")

    fun merge(existing: String?, players: List<PlayerState>, ports: Map<Int, Int>): String {
        // Drop every player's prior bindings first: a layout or port change leaves stale keys that
        // would otherwise linger and, sharing our single guid, cross-fire onto another player's port.
        val cleared = DolphinIni.removeKeys(existing, "[Controls]") { it.matches(PLAYER_KEY) }
        return DolphinIni.setKeys(cleared, "[Controls]", controlKeys(players, ports), assign = "=")
    }

    private fun controlKeys(players: List<PlayerState>, ports: Map<Int, Int>): Map<String, String> {
        val keys = LinkedHashMap<String, String>()
        players.forEach { player ->
            val index = player.player.index
            if (index !in 1..4) return@forEach
            val port = ports[index] ?: return@forEach
            val type = typeFor(player) ?: return@forEach
            val layout = layoutFor(player) ?: return@forEach
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

    private fun layoutFor(player: PlayerState): Layout? = when {
        player.hasPro || player.hasFullController -> FULL
        player.left != null && player.right == null -> LEFT_AS_PRO
        player.right != null && player.left == null -> RIGHT_AS_PRO
        else -> null
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
