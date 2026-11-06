package com.joegec.joycon2android.buttonmapping

/** What sets a Custom mapping apart from its closest layout: docs/architecture.md#button-mapping */
data class MappingChanges(
    val targets: Set<String> = emptySet(),
    val controller: Boolean = false,
    val thrust: Boolean = false,
    val sensitivity: Boolean = false,
) {
    val count: Int
        get() = targets.size + listOf(controller, thrust, sensitivity).count { it }
}
