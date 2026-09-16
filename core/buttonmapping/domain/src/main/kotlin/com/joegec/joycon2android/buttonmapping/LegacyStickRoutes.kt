package com.joegec.joycon2android.buttonmapping

/**
 * Older versions stored a target stick as one entry naming a whole physical stick
 * (`MainStick = LEFT_STICK`). Expands those into the four direction entries used now; a direction
 * the user has since set on its own keeps its explicit choice.
 */
internal fun Map<String, String>.withLegacyStickRoutesExpanded(): Map<String, String> {
    val legacy = filterValues { value -> StickSource.entries.any { it.name == value } }
    val expanded = legacy.flatMap { (target, stickName) ->
        MappingSource.directionsOf(StickSource.valueOf(stickName)).map { source ->
            stickDirectionKey(target, source.direction) to source.id
        }
    }.toMap()
    return expanded + (this - legacy.keys)
}
