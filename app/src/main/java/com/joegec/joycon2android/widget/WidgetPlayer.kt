package com.joegec.joycon2android.widget

import com.joegec.joycon2android.buttonmapping.PlayerBody

/** [shellColors]: 0xRRGGBB per controller held, left then right; null where the controller reports none. */
data class WidgetPlayer(
    val body: PlayerBody,
    val connectionLost: Boolean,
    val shellColors: List<Int?> = emptyList(),
)
