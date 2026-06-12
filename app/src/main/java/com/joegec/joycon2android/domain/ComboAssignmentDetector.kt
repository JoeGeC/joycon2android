package com.joegec.joycon2android.domain

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.Side

/**
 * Detects player-assignment button combos held on unassigned controllers,
 * mirroring the Switch "Change Grip/Order" screen:
 * - L held on one Joy-Con while R is held on another pairs both onto one player
 * - SL + SR held on a single Joy-Con assigns it solo (sideways)
 * - L + R held on a Pro Controller assigns it solo
 *
 * A controller that triggers a combo is latched until it releases all combo
 * buttons, so one held combo produces exactly one assignment.
 */
class ComboAssignmentDetector {

    private val latched = mutableSetOf<String>()

    fun detect(unassigned: List<ConnectedJoycon>): List<ComboAssignment> {
        unlatchReleased(unassigned)

        val candidates = unassigned.filter { it.ready && it.address !in latched }
        val solos = candidates.filter(::holdsSoloCombo)
        val pairs = pairLeftsWithRights(candidates - solos.toSet())

        val combos = solos.map { ComboAssignment(listOf(it.address)) } + pairs
        latched += combos.flatMap { it.addresses }
        return combos
    }

    private fun pairLeftsWithRights(candidates: List<ConnectedJoycon>): List<ComboAssignment> {
        val joycons = candidates.filter { it.side != Side.PRO }
        val lefts = joycons.filter { it.holds(JoyconButton.L) }
        val rights = joycons.filter { it.holds(JoyconButton.R) && it !in lefts }
        return lefts.zip(rights) { left, right ->
            ComboAssignment(listOf(left.address, right.address))
        }
    }

    private fun holdsSoloCombo(joycon: ConnectedJoycon): Boolean = when (joycon.side) {
        Side.PRO -> joycon.holds(JoyconButton.L) && joycon.holds(JoyconButton.R)
        else -> (joycon.holds(JoyconButton.SlLeft) && joycon.holds(JoyconButton.SrLeft)) ||
            (joycon.holds(JoyconButton.SlRight) && joycon.holds(JoyconButton.SrRight))
    }

    private fun unlatchReleased(unassigned: List<ConnectedJoycon>) {
        unassigned.forEach { joycon ->
            if (joycon.input.pressed.none { it in comboButtonIds }) latched -= joycon.address
        }
    }

    private fun ConnectedJoycon.holds(button: JoyconButton): Boolean = button.id in input.pressed

    private companion object {
        val comboButtonIds = setOf(
            JoyconButton.L, JoyconButton.R,
            JoyconButton.SlLeft, JoyconButton.SrLeft,
            JoyconButton.SlRight, JoyconButton.SrRight,
        ).map { it.id }.toSet()
    }
}
