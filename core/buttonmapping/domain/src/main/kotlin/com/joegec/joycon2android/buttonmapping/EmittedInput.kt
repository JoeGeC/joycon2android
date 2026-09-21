package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.SidewaysMapper

/**
 * The button the relay actually reports for this physical button. A lone Joy-Con is rotated
 * sideways by [SidewaysMapper] before its input leaves the app, so its d-pad arrives as face buttons.
 */
fun JoyconButton.emittedFor(side: JoyconSide): JoyconButton? {
    val emittedId = when (side) {
        JoyconSide.DUAL -> id
        JoyconSide.LEFT -> SidewaysMapper.remapButtonsLeft(setOf(id)).first()
        JoyconSide.RIGHT -> SidewaysMapper.remapButtonsRight(setOf(id)).first()
    }
    return JoyconButton.entries.firstOrNull { it.id == emittedId }
}

/** A lone Joy-Con's one stick is rotated onto the left-stick axes, whichever side it came from. */
fun MappingSource.Stick.emittedStick(side: JoyconSide): StickSource =
    if (side == JoyconSide.DUAL) stick else StickSource.LEFT_STICK

/**
 * The stick a target can read as a whole — keeping its analog range — when all four of its
 * directions follow the same emitted stick the natural way round; null when they're rearranged,
 * partly unbound, doubled up or mixed with buttons, which leaves each direction to be bound on its own.
 */
fun Map<StickDirection, List<MappingSource>>.wholeEmittedStick(side: JoyconSide): StickSource? {
    val sticks = StickDirection.entries.map { direction ->
        val source = this[direction]?.singleOrNull() as? MappingSource.Stick ?: return null
        if (source.direction != direction) return null
        source.emittedStick(side)
    }
    return sticks.distinct().singleOrNull()
}
