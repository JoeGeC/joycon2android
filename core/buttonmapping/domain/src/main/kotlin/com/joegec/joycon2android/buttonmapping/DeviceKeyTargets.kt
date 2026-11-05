package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.KeyboardKey
import com.joegec.joycon2android.model.MouseAction

// Prefixed so neither collides with an emulator target such as `SwitchProButton.Home`.
val AndroidKey.targetKey: String get() = "ANDROID_$name"
val KeyboardKey.targetKey: String get() = "KEYBOARD_$name"
val MouseAction.targetKey: String get() = "MOUSE_$name"

/** Its sources form one [HoldCombo]: docs/virtual-gamepad.md#mouse-on-and-off */
const val MOUSE_MODE_KEY = "MOUSE_MODE"

/** Targets the app presses itself rather than an emulator, so a grip switch keeps them. */
val DEVICE_KEY_TARGETS: Set<String> =
    (AndroidKey.entries.map { it.targetKey } + KeyboardKey.entries.map { it.targetKey } + MouseAction.entries.map { it.targetKey })
        .toSet() + MOUSE_MODE_KEY + JOYCON_MOUSE_KEY

/** Only buttons press an Android key; a stick direction in the entry is ignored. */
fun Map<String, String>.androidKeyBindings(): KeyBindings<AndroidKey> =
    keyBindings(AndroidKey.entries, { it.targetKey }) { it as? MappingSource.Button }

/** [sidewaysRemote] says which way the mapping's stick directions face: docs/dsu-motion.md#playing-as-a-sideways-wii-remote */
fun Map<String, String>.keyboardKeyBindings(side: JoyconSide, sidewaysRemote: Boolean): KeyBindings<KeyboardKey> =
    keyBindings(KeyboardKey.entries, { it.targetKey }) { it.inHardwareAxes(side, sidewaysRemote) }

fun Map<String, String>.mouseActionBindings(side: JoyconSide, sidewaysRemote: Boolean): KeyBindings<MouseAction> =
    keyBindings(MouseAction.entries, { it.targetKey }) { it.inHardwareAxes(side, sidewaysRemote) }

/** Keyed by the combo itself, so merging consoles' bindings keeps each combo whole. */
fun Map<String, String>.mouseModeCombos(side: JoyconSide, sidewaysRemote: Boolean): KeyBindings<HoldCombo> {
    val sources = this[MOUSE_MODE_KEY].orEmpty().toMappingSources().map { it.inHardwareAxes(side, sidewaysRemote) }
    return if (sources.isEmpty()) KeyBindings() else HoldCombo(sources).let { KeyBindings(mapOf(it to it.sources)) }
}

private fun MappingSource.inHardwareAxes(side: JoyconSide, sidewaysRemote: Boolean): MappingSource = when (this) {
    is MappingSource.Button -> this
    is MappingSource.Stick -> copy(direction = hardwareDirection(side, sidewaysRemote))
}

private fun <K> Map<String, String>.keyBindings(
    keys: List<K>,
    targetKey: (K) -> String,
    accept: (MappingSource) -> MappingSource?,
) = KeyBindings(
    keys.mapNotNull { key ->
        val sources = this[targetKey(key)].orEmpty().toMappingSources().mapNotNull(accept)
        sources.takeIf { it.isNotEmpty() }?.let { key to it }
    }.toMap(),
)
