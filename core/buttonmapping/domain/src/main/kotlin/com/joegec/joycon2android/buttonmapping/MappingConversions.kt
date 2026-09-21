package com.joegec.joycon2android.buttonmapping

/** The storage key for one direction of a target stick, e.g. `MainStick_UP`. */
fun Enum<*>.directionKey(direction: StickDirection): String = stickDirectionKey(name, direction)

internal fun stickDirectionKey(targetName: String, direction: StickDirection) = "${targetName}_${direction.name}"

/**
 * Recovers a typed target -> sources map from the repository's opaque string map, silently dropping
 * entries whose key isn't a [T] and sources that are no longer known — a stale or "None"-selected
 * entry simply produces no binding rather than a crash.
 */
inline fun <reified T : Enum<T>> Map<String, String>.toSourceMap(): Map<T, List<MappingSource>> =
    mapNotNull { (key, value) ->
        val target = enumValues<T>().firstOrNull { it.name == key } ?: return@mapNotNull null
        val sources = value.toMappingSources().ifEmpty { return@mapNotNull null }
        target to sources
    }.toMap()

/** Same recovery as [toSourceMap], for the four direction entries of each target stick. */
inline fun <reified T : Enum<T>> Map<String, String>.toStickDirectionMap(): Map<T, Map<StickDirection, List<MappingSource>>> =
    enumValues<T>().associateWith { target ->
        StickDirection.entries.mapNotNull { direction ->
            this[target.directionKey(direction)]?.toMappingSources()?.ifEmpty { null }?.let { direction to it }
        }.toMap()
    }.filterValues { it.isNotEmpty() }
