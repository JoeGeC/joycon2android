package com.joegec.joycon2android.buttonmapping

/** One player's whole mapping, frozen — every binding, not a reference to a layout that can go. */
data class PlayerLayoutSnapshot(
    val body: PlayerBody,
    val entries: Map<String, String>,
    val sidewaysRemote: Boolean,
)
