package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerState

// Calibrated: docs/protocol.md#stick-range-and-centre. Up and right read high.
private const val CENTER = 2048
private const val HALF_SPAN = 2047f

/** How far [stick] leans towards [direction], in the hardware's own axes: 0 at rest, 1 at the gate. */
fun PlayerState.tilt(stick: StickSource, direction: StickDirection): Float {
    val (x, y) = when (stick) {
        StickSource.LEFT_STICK -> leftStickX to leftStickY
        StickSource.RIGHT_STICK -> rightStickX to rightStickY
    }
    val lean = when (direction) {
        StickDirection.UP -> y - CENTER
        StickDirection.DOWN -> CENTER - y
        StickDirection.RIGHT -> x - CENTER
        StickDirection.LEFT -> CENTER - x
    }
    return (lean / HALF_SPAN).coerceIn(0f, 1f)
}

/** [stick] with [direction]'s half of its axis held at centre, the other half untouched. */
fun PlayerState.withoutTilt(stick: StickSource, direction: StickDirection): PlayerState = when {
    stick == StickSource.RIGHT_STICK && hasPro -> copy(
        left = left?.withInput { it.copy(rightStickX = it.rightStickX.cutX(direction), rightStickY = it.rightStickY.cutY(direction)) },
    )
    stick == StickSource.RIGHT_STICK -> copy(right = right?.withoutOwnTilt(direction))
    else -> copy(left = left?.withoutOwnTilt(direction))
}

private fun ConnectedJoycon.withoutOwnTilt(direction: StickDirection) =
    withInput { it.copy(stickX = it.stickX.cutX(direction), stickY = it.stickY.cutY(direction)) }

private fun ConnectedJoycon.withInput(change: (JoyconInput) -> JoyconInput) = copy(input = change(input))

private fun Int.cutX(direction: StickDirection): Int = when (direction) {
    StickDirection.RIGHT -> coerceAtMost(CENTER)
    StickDirection.LEFT -> coerceAtLeast(CENTER)
    StickDirection.UP, StickDirection.DOWN -> this
}

private fun Int.cutY(direction: StickDirection): Int = when (direction) {
    StickDirection.UP -> coerceAtMost(CENTER)
    StickDirection.DOWN -> coerceAtLeast(CENTER)
    StickDirection.LEFT, StickDirection.RIGHT -> this
}
