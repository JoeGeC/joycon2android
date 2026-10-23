package com.joegec.joycon2android.buttonmapping.presentation

/** Android keys need Shizuku only over DSU, as the pad carries them; the keyboard and mouse always do. */
data class DeviceKeyWarnings(
    val androidNeedsShizuku: Boolean = false,
    val keyboardAndMouseNeedShizuku: Boolean = false,
)
