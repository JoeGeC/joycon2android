package com.joegec.joycon2android.emulatorconfig

import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.model.PlayerState

/**
 * The vocabulary of Eden's `config.ini` `[Controls]` section, shared by the two features that can
 * bind a player there — the Virtual Gamepad as an Android HID pad, DSU as a cemuhook pad. Both
 * write the same keys for the same player; only the device half of each binding differs.
 */
object EdenControls {
    const val SECTION = "[Controls]"

    /** Every player key, so either feature can clear a prior layout before rewriting it. */
    val PLAYER_KEY = Regex("""player_\d+_.*""")

    val BUTTON_KEYS = mapOf(
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

    val STICK_KEYS = mapOf(SwitchProStick.LStick to "lstick", SwitchProStick.RStick to "rstick")

    /**
     * Eden's npad type. Both single Joy-Cons report as Pro: Eden doesn't translate a sideways
     * Joy-Con — it only sets an `is_horizontal` flag and masks an npad by type, so a JoyconLeft
     * can't even report A/B/X/Y — so a normalised full controller is presented and the rotation
     * done on our side instead.
     */
    fun npadType(player: PlayerState): Int? = when {
        player.hasPro -> PRO
        player.hasFullController -> DUAL_JOYCON
        player.hasController -> PRO
        else -> null
    }

    fun quote(value: String) = "\"$value\""

    /**
     * A stick assembled from up to four digital inputs, each a whole binding of its own. Eden parses
     * the nested bindings out of one value, so their `:`, `,` and `$` are escaped as `$0`, `$1` and
     * `$2`, exactly as its ParamPackage serializes them.
     */
    fun stickFromButtons(directions: Map<StickDirection, String>): String =
        (listOf("engine:analog_from_button") + directions.map { (direction, binding) ->
            "${direction.name.lowercase()}:${escapeNested(binding)}"
        }).joinToString(",")

    private fun escapeNested(binding: String) =
        binding.replace("$", "$2").replace(":", "$0").replace(",", "$1")

    private const val PRO = 0
    private const val DUAL_JOYCON = 1
}

/** Eden falls back to the engine default when a key's `\default` flag is true, so pin both. */
fun MutableMap<String, String>.defineEdenKey(key: String, value: String) {
    this[key] = value
    this["$key\\default"] = "false"
}
