package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.ButtonBindings
import com.joegec.joycon2android.model.KeyboardKey

// Prefixed so neither collides with an emulator target such as `SwitchProButton.Home`.
val AndroidKey.targetKey: String get() = "ANDROID_$name"
val KeyboardKey.targetKey: String get() = "KEYBOARD_$name"

/** Targets the app presses itself rather than an emulator, so a grip switch keeps them. */
val DEVICE_KEY_TARGETS: Set<String> =
    (AndroidKey.entries.map { it.targetKey } + KeyboardKey.entries.map { it.targetKey }).toSet()

fun Map<String, String>.androidKeyBindings(): ButtonBindings<AndroidKey> = buttonBindings(AndroidKey.entries) { it.targetKey }

fun Map<String, String>.keyboardKeyBindings(): ButtonBindings<KeyboardKey> = buttonBindings(KeyboardKey.entries) { it.targetKey }

/** Only buttons press a key; a stick direction in the entry is ignored. */
private fun <K> Map<String, String>.buttonBindings(keys: List<K>, targetKey: (K) -> String) = ButtonBindings(
    keys.mapNotNull { key ->
        val buttons = this[targetKey(key)].orEmpty().toMappingSources()
            .filterIsInstance<MappingSource.Button>()
            .mapTo(mutableSetOf()) { it.button }
        buttons.takeIf { it.isNotEmpty() }?.let { key to it }
    }.toMap(),
)
