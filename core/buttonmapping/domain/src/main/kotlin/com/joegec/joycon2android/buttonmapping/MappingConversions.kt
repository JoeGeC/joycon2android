package com.joegec.joycon2android.buttonmapping

/** The storage key for one direction of a target stick, e.g. `MainStick_UP`. */
fun Enum<*>.directionKey(direction: StickDirection): String = stickDirectionKey(name, direction)

internal fun stickDirectionKey(targetName: String, direction: StickDirection) = "${targetName}_${direction.name}"

/**
 * Recovers a typed target -> source map from the repository's opaque string map, silently dropping
 * entries whose key isn't a [T] or whose value isn't a known source — a stale or "None"-selected
 * entry simply produces no binding rather than a crash.
 */
inline fun <reified T : Enum<T>> Map<String, String>.toSourceMap(): Map<T, MappingSource> =
    mapNotNull { (key, value) ->
        val target = enumValues<T>().firstOrNull { it.name == key } ?: return@mapNotNull null
        val source = MappingSource.fromId(value) ?: return@mapNotNull null
        target to source
    }.toMap()

/** Same recovery as [toSourceMap], for the four direction entries of each target stick. */
inline fun <reified T : Enum<T>> Map<String, String>.toStickDirectionMap(): Map<T, Map<StickDirection, MappingSource>> =
    enumValues<T>().associateWith { target ->
        StickDirection.entries.mapNotNull { direction ->
            this[target.directionKey(direction)]?.let(MappingSource::fromId)?.let { direction to it }
        }.toMap()
    }.filterValues { it.isNotEmpty() }
