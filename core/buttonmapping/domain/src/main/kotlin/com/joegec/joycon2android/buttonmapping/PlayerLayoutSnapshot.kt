package com.joegec.joycon2android.buttonmapping

data class PlayerLayoutSnapshot(
    val body: PlayerBody,
    val entries: Map<String, String>,
    val sidewaysRemote: Boolean,
)
