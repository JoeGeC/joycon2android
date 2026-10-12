package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AndroidKeyBindings

/** Prefixed so it never collides with an emulator target such as `SwitchProButton.Home`. */
val AndroidKey.targetKey: String get() = "ANDROID_$name"

/** Only buttons fire an Android key; a stick direction in the entry is ignored. */
fun Map<String, String>.androidKeyBindings(): AndroidKeyBindings = AndroidKeyBindings(
    AndroidKey.entries.mapNotNull { key ->
        val buttons = this[key.targetKey].orEmpty().toMappingSources()
            .filterIsInstance<MappingSource.Button>()
            .mapTo(mutableSetOf()) { it.button }
        buttons.takeIf { it.isNotEmpty() }?.let { key to it }
    }.toMap(),
)
