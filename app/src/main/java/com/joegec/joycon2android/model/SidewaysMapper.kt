package com.joegec.joycon2android.model

/**
 * Transforms raw Joy-Con input into gamepad-oriented values for sideways (single Joy-Con) mode.
 *
 * Left Joy-Con rotated 90° CCW: stick and d-pad axes rotate accordingly.
 * Right Joy-Con rotated 90° CW: stick axes rotate in the opposite direction.
 * SL/SR rail buttons map to the opposite side's shoulder buttons, giving a full L/R/ZL/ZR set.
 */
object SidewaysMapper {

    private const val STICK_MAX = 4096

    fun rotateStickLeft(rawX: Int, rawY: Int): Pair<Int, Int> =
        (STICK_MAX - rawY) to rawX

    fun rotateStickRight(rawX: Int, rawY: Int): Pair<Int, Int> =
        rawY to (STICK_MAX - rawX)

    fun remapButtonsLeft(pressed: Set<String>): Set<String> =
        pressed.mapTo(mutableSetOf()) { LEFT_REMAP[it] ?: it }

    fun remapButtonsRight(pressed: Set<String>): Set<String> =
        pressed.mapTo(mutableSetOf()) { RIGHT_REMAP[it] ?: it }

    // D-pad rotates 90° CCW; rail buttons fill the missing shoulder pair
    private val LEFT_REMAP = mapOf(
        "Right" to "Up",
        "Left" to "Down",
        "Up" to "Left",
        "Down" to "Right",
        "SL(L)" to "R",
        "SR(L)" to "ZR",
    )

    // Stick click becomes LS (since the stick maps to left position); rail buttons fill missing shoulders
    private val RIGHT_REMAP = mapOf(
        "RS" to "LS",
        "SL(R)" to "L",
        "SR(R)" to "ZL",
    )
}
