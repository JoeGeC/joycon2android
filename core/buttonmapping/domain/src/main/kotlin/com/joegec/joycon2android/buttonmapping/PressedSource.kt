package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState

private const val PRESS_TILT = 0.5f

/**
 * What this frame starts pressing that [previous] didn't: a button, else the stick direction leaned
 * furthest past half-way. Directions come in the frame the mapping stores them in.
 */
fun PlayerState.sourcePressedSince(previous: PlayerState, side: JoyconSide, heldSideways: Boolean): MappingSource? =
    newlyPressedButton(previous) ?: newlyLeanedStick(previous, side, heldSideways)

/** No button down and no stick past where it would press. */
fun PlayerState.pressesNothing(): Boolean =
    pressed.isEmpty() && StickSource.entries.flatMap(MappingSource::directionsOf).none { tilt(it.stick, it.direction) >= PRESS_TILT }

private fun PlayerState.newlyPressedButton(previous: PlayerState): MappingSource? {
    val pressedNow = pressed - previous.pressed
    return JoyconButton.entries.firstOrNull { it.id in pressedNow }?.let(MappingSource::Button)
}

private fun PlayerState.newlyLeanedStick(previous: PlayerState, side: JoyconSide, heldSideways: Boolean): MappingSource? =
    StickSource.entries.flatMap(MappingSource::directionsOf)
        .filter { tilt(it.stick, it.direction) >= PRESS_TILT && previous.tilt(it.stick, it.direction) < PRESS_TILT }
        .maxByOrNull { tilt(it.stick, it.direction) }
        ?.inMappingFrame(side, heldSideways)

private fun MappingSource.Stick.inMappingFrame(side: JoyconSide, heldSideways: Boolean): MappingSource.Stick =
    StickDirection.entries
        .map { copy(direction = it) }
        .first { it.hardwareDirection(side, heldSideways) == direction }
