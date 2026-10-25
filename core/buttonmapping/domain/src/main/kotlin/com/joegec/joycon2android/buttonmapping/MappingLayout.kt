package com.joegec.joycon2android.buttonmapping

/** Shipped or saved; entries are in the repository's string form. docs/architecture.md#button-mapping */
interface MappingLayout {
    val id: String

    /** docs/dsu-motion.md#wii-controller */
    val wiiController: WiiController get() = WiiController.REMOTE

    /** docs/dsu-motion.md#thrusts */
    val thrust: Boolean get() = false

    /** docs/dsu-motion.md#motion-sensitivity */
    val sensitivityPercent: Int get() = RemoteMotion.DEFAULT_SENSITIVITY_PERCENT

    fun motion(side: JoyconSide) = RemoteMotion(wiiController.heldBy(side), thrust, sensitivityPercent)

    fun entries(side: JoyconSide): Map<String, String>
}
