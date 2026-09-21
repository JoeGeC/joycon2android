package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.directionKey
import com.joegec.joycon2android.buttonmapping.toSourceId
import com.joegec.joycon2android.model.JoyconButton

internal fun <K : Enum<K>> Map<K, JoyconButton>.buttonEntries(): Map<String, String> =
    entries.associate { (target, button) -> target.name to button.name }

internal fun <K : Enum<K>> Map<K, List<MappingSource>>.sourceEntries(): Map<String, String> =
    entries.associate { (target, sources) -> target.name to sources.toSourceId() }

internal fun <K : Enum<K>> Map<K, StickSource>.stickEntries(): Map<String, String> =
    entries.flatMap { (target, stick) ->
        MappingSource.directionsOf(stick).map { target.directionKey(it.direction) to it.id }
    }.toMap()

/** Leaves the layout alone unless both targets are bound, so a preset can't lose one to a swap. */
internal fun Map<String, String>.swappingSources(first: Enum<*>, second: Enum<*>): Map<String, String> {
    val firstSource = this[first.name] ?: return this
    val secondSource = this[second.name] ?: return this
    return this + mapOf(first.name to secondSource, second.name to firstSource)
}
