package com.joegec.joycon2android.buttonmapping

data class PlayerMapping(
    val body: PlayerBody,
    val entries: Map<String, String>,
    val motion: RemoteMotion,
    /** Null is "Custom". */
    val layout: MappingLayout?,
) {
    fun snapshot() = PlayerLayoutSnapshot(body, entries, motion)
}
