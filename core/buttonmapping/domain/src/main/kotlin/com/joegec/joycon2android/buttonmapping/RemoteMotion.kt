package com.joegec.joycon2android.buttonmapping

/** How a Wii Remote's motion is shaped, beyond its bindings. docs/dsu-motion.md#dolphin-wii-remote-mapping */
data class RemoteMotion(
    val sideways: Boolean = false,
    val thrust: Boolean = false,
    val sensitivityPercent: Int = DEFAULT_SENSITIVITY_PERCENT,
) {
    companion object {
        const val UNSCALED_SENSITIVITY_PERCENT = 100

        // Wii Sports swings read slightly soft unscaled: docs/dsu-motion.md#motion-sensitivity
        const val DEFAULT_SENSITIVITY_PERCENT = 110
        val SENSITIVITY_PERCENT_RANGE = 50..200
        const val SENSITIVITY_PERCENT_STEP = 5
    }
}
