package com.joegec.joycon2android.buttonmapping

/** Expands an older whole-stick entry (`MainStick = LEFT_STICK`) into four; an explicit direction wins. */
internal fun Map<String, String>.withLegacyStickRoutesExpanded(): Map<String, String> {
    val legacy = filterValues { value -> StickSource.entries.any { it.name == value } }
    val expanded = legacy.flatMap { (target, stickName) ->
        MappingSource.directionsOf(StickSource.valueOf(stickName)).map { source ->
            stickDirectionKey(target, source.direction) to source.id
        }
    }.toMap()
    return expanded + (this - legacy.keys)
}
