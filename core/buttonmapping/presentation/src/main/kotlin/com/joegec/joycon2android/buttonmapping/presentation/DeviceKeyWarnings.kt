package com.joegec.joycon2android.buttonmapping.presentation

import com.joegec.joycon2android.buttonmapping.PlayerBody

/** Android keys need Shizuku only over DSU, as the pad carries them; the keyboard and mouse always do. */
data class DeviceKeyWarnings(
    val androidNeedsShizuku: Boolean = false,
    val keyboardAndMouseNeedShizuku: Boolean = false,
    /** Held by a controller without the Joy-Con 2's mouse sensor. */
    val pointerlessBodies: Set<PlayerBody> = emptySet(),
)
