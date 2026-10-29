package com.joegec.joycon2android.model

/**
 * Each motor's strength, 0 to 1: the strong low-frequency motor and the weak high-frequency one. A game
 * gives its full range; scaled by a player's rumble strength, 1 is the actuator's safe maximum.
 */
data class Rumble(val strong: Float, val weak: Float) {
    val isOn: Boolean get() = strong > 0f || weak > 0f

    fun scaledBy(factor: Float) = Rumble(strong * factor, weak * factor)

    companion object {
        val OFF = Rumble(0f, 0f)
    }
}
