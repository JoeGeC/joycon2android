package com.joegec.joycon2android.emulatorconfig

import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.model.PlayerState

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

    /** Both single Joy-Cons report as Pro: docs/virtual-gamepad.md#why-theyre-set-up-as-pro-controllers. */
    fun npadType(player: PlayerState): Int? = when {
        player.hasPro -> PRO
        player.hasFullController -> DUAL_JOYCON
        player.hasController -> PRO
        else -> null
    }

    fun quote(value: String) = "\"$value\""

    /** A stick built from up to four whole bindings nested in one value, escaped as Eden's
     * ParamPackage serializes them: docs/virtual-gamepad.md#emulator-config. */
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
