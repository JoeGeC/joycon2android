package com.joegec.joycon2android.buttonmapping

private const val SOURCE_SEPARATOR = "|"

/** Any of several sources fires a target, so a value joins their ids; an older single id reads as one. */
fun sourceIdsOf(value: String): List<String> = value.split(SOURCE_SEPARATOR).filter { it.isNotEmpty() }

fun sourceIdOf(ids: List<String>): String = ids.joinToString(SOURCE_SEPARATOR)

fun String.toMappingSources(): List<MappingSource> = sourceIdsOf(this).mapNotNull(MappingSource::fromId)

fun List<MappingSource>.toSourceId(): String = sourceIdOf(map { it.id })
