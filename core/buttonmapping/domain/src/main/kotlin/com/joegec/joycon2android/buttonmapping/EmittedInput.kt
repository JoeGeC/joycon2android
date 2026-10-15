package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.AnalogTriggerMapper
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.SidewaysMapper
import com.joegec.joycon2android.model.SingleJoyconMapper

/** What the relay reports for this button once [SidewaysMapper], [SingleJoyconMapper] or [AnalogTriggerMapper] has moved it. */
fun JoyconButton.emittedFor(side: JoyconSide, singleJoycon: Boolean = false): JoyconButton? {
    val emittedId =
        (if (singleJoycon && side.isLone) SingleJoyconMapper.remapRails(setOf(id)).first() else emittedId(side))
            ?: return null
    return JoyconButton.entries.firstOrNull { it.id == emittedId }
}

private fun JoyconButton.emittedId(side: JoyconSide): String? = when (side) {
    JoyconSide.DUAL -> id
    JoyconSide.GAMECUBE -> AnalogTriggerMapper.emittedId(id)
    JoyconSide.LEFT -> SidewaysMapper.remapButtonsLeft(setOf(id)).first()
    JoyconSide.RIGHT -> SidewaysMapper.remapButtonsRight(setOf(id)).first()
}

/** A sideways Joy-Con's one stick is rotated onto the left-stick axes; a single Joy-Con's stays its own. */
fun MappingSource.Stick.emittedStick(side: JoyconSide, singleJoycon: Boolean = false): StickSource =
    if (side.isLone && !singleJoycon) StickSource.LEFT_STICK else stick

/** The relay reports a lone Joy-Con's up as its rail; off a sideways remote, up is its L/R edge. */
fun MappingSource.Stick.emittedDirection(side: JoyconSide, sidewaysRemote: Boolean): StickDirection = when {
    sidewaysRemote -> direction
    side == JoyconSide.LEFT -> UPRIGHT_LEFT.getValue(direction)
    side == JoyconSide.RIGHT -> UPRIGHT_RIGHT.getValue(direction)
    else -> direction
}

private val UPRIGHT_LEFT = mapOf(
    StickDirection.UP to StickDirection.LEFT,
    StickDirection.RIGHT to StickDirection.UP,
    StickDirection.DOWN to StickDirection.RIGHT,
    StickDirection.LEFT to StickDirection.DOWN,
)

private val UPRIGHT_RIGHT = mapOf(
    StickDirection.UP to StickDirection.RIGHT,
    StickDirection.LEFT to StickDirection.UP,
    StickDirection.DOWN to StickDirection.LEFT,
    StickDirection.RIGHT to StickDirection.DOWN,
)

/** Non-null only when all four directions follow one stick the natural way round, so it stays analog. */
fun Map<StickDirection, List<MappingSource>>.wholeEmittedStick(
    side: JoyconSide,
    singleJoycon: Boolean = false,
): StickSource? {
    val sticks = StickDirection.entries.map { direction ->
        val source = this[direction]?.singleOrNull() as? MappingSource.Stick ?: return null
        if (source.direction != direction) return null
        source.emittedStick(side, singleJoycon)
    }
    return sticks.distinct().singleOrNull()
}
