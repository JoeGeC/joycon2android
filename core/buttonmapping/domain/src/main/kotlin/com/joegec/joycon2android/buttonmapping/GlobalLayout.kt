package com.joegec.joycon2android.buttonmapping

/**
 * Every player's mapping saved together under one name. A set is bound to the bodies it was saved
 * from — a mapping written for a lone Joy-Con says nothing about a pair — so it can only be
 * restored onto the same players holding the same bodies.
 */
data class GlobalLayout(
    val id: String,
    val name: String,
    val console: Console,
    val bodies: List<PlayerLayoutSnapshot>,
) {
    fun fits(bodies: List<PlayerBody>): Boolean = this.bodies.map { it.body }.toSet() == bodies.toSet()
}
