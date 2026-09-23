package com.joegec.joycon2android.buttonmapping

/** Restores only onto the same players holding the same bodies it was saved from. */
data class GlobalLayout(
    val id: String,
    val name: String,
    val console: Console,
    val bodies: List<PlayerLayoutSnapshot>,
) {
    fun fits(bodies: List<PlayerBody>): Boolean = this.bodies.map { it.body }.toSet() == bodies.toSet()
}
