package com.joegec.joycon2android.buttonmapping

/** What a Wii player holds and how its motion is shaped, beyond its bindings. docs/dsu-motion.md#dolphin-wii-remote-mapping */
data class RemoteMotion(
    val controller: WiiController = WiiController.REMOTE,
    val thrust: Boolean = false,
    val sensitivityPercent: Int = DEFAULT_SENSITIVITY_PERCENT,
) {
    val heldSideways: Boolean get() = controller.heldSideways

    companion object {
        const val UNSCALED_SENSITIVITY_PERCENT = 100

        // Wii Sports swings read slightly soft unscaled: docs/dsu-motion.md#motion-sensitivity
        const val DEFAULT_SENSITIVITY_PERCENT = 110
        val SENSITIVITY_PERCENT_RANGE = 50..200
        const val SENSITIVITY_PERCENT_STEP = 5
    }
}
