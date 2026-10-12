package com.joegec.joycon2android.buttonmapping

/** How a Wii Remote's motion is shaped, beyond its bindings. docs/dsu-motion.md#dolphin-wii-remote-mapping */
data class RemoteMotion(
    val sideways: Boolean = false,
    val thrust: Boolean = false,
)
