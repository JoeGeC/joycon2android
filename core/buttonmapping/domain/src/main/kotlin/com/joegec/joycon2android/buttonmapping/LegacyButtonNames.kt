package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton

private val renamedButtons = mapOf("Camera" to JoyconButton.Capture.name)

/** Older versions stored some sources under a button's former name; maps those onto the current one. */
internal fun Map<String, String>.withLegacyButtonNamesRenamed(): Map<String, String> =
    mapValues { (_, source) -> renamedButtons[source] ?: source }
