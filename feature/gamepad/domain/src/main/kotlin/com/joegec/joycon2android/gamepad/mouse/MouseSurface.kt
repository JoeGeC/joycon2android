package com.joegec.joycon2android.gamepad.mouse

import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side

// Raw accel reads +1 g (4096) on the axis pointing up; this allows a 45° lean.
private const val RAIL_DOWN_ACCEL = 2896

/** Fingers round the rail read as a surface too; only a desk turns the rail to the floor. */
fun JoyconInput.restsOnSurface(side: Side): Boolean {
    if (mouse?.onSurface != true) return false
    val awayFromRail = when (side) {
        Side.LEFT -> -accelX
        Side.RIGHT -> accelX
        Side.PRO, Side.UNKNOWN -> return false
    }
    return awayFromRail >= RAIL_DOWN_ACCEL
}

/** docs/virtual-gamepad.md#mouse */
val PlayerState.spendsButtonsOnMouse: Boolean
    get() = !canMovePointer || listOfNotNull(left, right).any { it.input.restsOnSurface(it.side) }
