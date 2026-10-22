package com.joegec.joycon2android.model

/** Field meanings and measurements: docs/protocol.md#mouse-0x100x17 */
data class MouseSensor(val x: Int, val y: Int, val distance: Int) {
    val onSurface: Boolean get() = distance < LIFTED_DISTANCE

    private companion object {
        // ~150 on a desk, ~3100 lifted.
        const val LIFTED_DISTANCE = 1000
    }
}
