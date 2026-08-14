package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton

/**
 * Recovers a typed target -> physical-button map from the repository's opaque string map,
 * silently dropping entries whose key isn't a [T] or whose value isn't a real [JoyconButton] —
 * a stale or "None"-selected entry simply produces no binding rather than a crash.
 */
inline fun <reified T : Enum<T>> Map<String, String>.toButtonMap(): Map<T, JoyconButton> =
    mapNotNull { (key, value) ->
        val target = enumValues<T>().firstOrNull { it.name == key } ?: return@mapNotNull null
        val button = runCatching { enumValueOf<JoyconButton>(value) }.getOrNull() ?: return@mapNotNull null
        target to button
    }.toMap()

/** Same recovery as [toButtonMap], for the stick-routing entries of a mapping. */
inline fun <reified T : Enum<T>> Map<String, String>.toStickMap(): Map<T, StickSource> =
    mapNotNull { (key, value) ->
        val target = enumValues<T>().firstOrNull { it.name == key } ?: return@mapNotNull null
        val source = runCatching { enumValueOf<StickSource>(value) }.getOrNull() ?: return@mapNotNull null
        target to source
    }.toMap()
