package com.joegec.joycon2android.buttonmapping

private const val SOURCE_SEPARATOR = "|"

/**
 * Several sources can drive one target — any of them fires it — so a stored value holds their ids
 * joined together. A value written by an older version is a single id, which reads back as one source.
 */
fun sourceIdsOf(value: String): List<String> = value.split(SOURCE_SEPARATOR).filter { it.isNotEmpty() }

fun sourceIdOf(ids: List<String>): String = ids.joinToString(SOURCE_SEPARATOR)

fun String.toMappingSources(): List<MappingSource> = sourceIdsOf(this).mapNotNull(MappingSource::fromId)

fun List<MappingSource>.toSourceId(): String = sourceIdOf(map { it.id })
