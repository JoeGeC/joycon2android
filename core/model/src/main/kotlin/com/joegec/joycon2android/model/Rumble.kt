package com.joegec.joycon2android.model

/** Each motor's strength, 0 to 1: the strong low-frequency motor and the weak high-frequency one. */
data class Rumble(val strong: Float, val weak: Float) {
    val isOn: Boolean get() = strong > 0f || weak > 0f

    companion object {
        val OFF = Rumble(0f, 0f)
    }
}
