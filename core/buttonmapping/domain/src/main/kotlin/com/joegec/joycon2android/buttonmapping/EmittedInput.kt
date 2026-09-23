package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.SidewaysMapper

/** What the relay reports for this button once [SidewaysMapper] has rotated a lone Joy-Con. */
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

/** Non-null only when all four directions follow one stick the natural way round, so it stays analog. */
fun Map<StickDirection, List<MappingSource>>.wholeEmittedStick(side: JoyconSide): StickSource? {
    val sticks = StickDirection.entries.map { direction ->
        val source = this[direction]?.singleOrNull() as? MappingSource.Stick ?: return null
        if (source.direction != direction) return null
        source.emittedStick(side)
    }
    return sticks.distinct().singleOrNull()
}
