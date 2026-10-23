package com.joegec.joycon2android.buttonmapping.presentation

/** Android keys need Shizuku only over DSU, as the pad carries them; the keyboard always does. */
data class DeviceKeyWarnings(
    val androidNeedsShizuku: Boolean = false,
    val keyboardNeedsShizuku: Boolean = false,
)
