package com.joegec.joycon2android.model

/**
 * Transforms raw Joy-Con input into gamepad-oriented values for sideways (single Joy-Con) mode.
 *
 * Left Joy-Con rotated 90° CCW, right Joy-Con 90° CW, matching how the Switch treats a lone
 * Joy-Con: the stick axes rotate with the body, the four-button cluster becomes the face buttons in
 * its rotated positions, and the SL/SR rail buttons fill in the shoulder pair the body lacks.
 *
 * The cluster must land on real face buttons rather than the HID hat. A sideways Joy-Con has no
 * d-pad, so a virtual pad that reports its cluster as hat directions has no A/B/X/Y at all, and
 * anything binding a hat axis without its sign — Eden's manual mapping does exactly this — cannot
 * tell left from right or up from down.
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

    // D-pad rotates 90° CCW onto the faces: Right sits at the top, Down at the right, Left at the
    // bottom, Up at the left. Rail buttons fill the missing right-hand shoulders.
    private val LEFT_REMAP = mapOf(
        JoyconButton.Right.id to JoyconButton.X.id,
        JoyconButton.Down.id to JoyconButton.A.id,
        JoyconButton.Left.id to JoyconButton.B.id,
        JoyconButton.Up.id to JoyconButton.Y.id,
        JoyconButton.SlLeft.id to JoyconButton.R.id,
        JoyconButton.SrLeft.id to JoyconButton.ZR.id,
    )

    // Faces rotate 90° CW onto themselves: Y sits at the top, X at the right, A at the bottom, B at
    // the left. Stick click becomes LS (the stick is now the left one); rails fill the left shoulders.
    private val RIGHT_REMAP = mapOf(
        JoyconButton.Y.id to JoyconButton.X.id,
        JoyconButton.X.id to JoyconButton.A.id,
        JoyconButton.A.id to JoyconButton.B.id,
        JoyconButton.B.id to JoyconButton.Y.id,
        JoyconButton.RS.id to JoyconButton.LS.id,
        JoyconButton.SlRight.id to JoyconButton.L.id,
        JoyconButton.SrRight.id to JoyconButton.ZL.id,
    )
}
