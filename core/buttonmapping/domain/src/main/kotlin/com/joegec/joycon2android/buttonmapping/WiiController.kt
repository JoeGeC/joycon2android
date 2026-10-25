package com.joegec.joycon2android.buttonmapping

/** What a Wii player holds: docs/dsu-motion.md#wii-controller */
enum class WiiController {
    REMOTE,
    SIDEWAYS_REMOTE,
    CLASSIC,
    ;

    /** A lone Joy-Con plays a Classic Controller sideways too, so its stick and motion turn the same way. */
    val heldSideways: Boolean get() = this != REMOTE

    /** Only a lone Joy-Con turns sideways, so a layout's Sideways is a pair's plain remote. */
    fun heldBy(side: JoyconSide): WiiController = if (this == SIDEWAYS_REMOTE && !side.isLone) REMOTE else this
}
