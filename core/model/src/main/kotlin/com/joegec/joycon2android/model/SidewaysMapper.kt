package com.joegec.joycon2android.model

/** docs/virtual-gamepad.md#sidewaysmapper */
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

    // The cluster must land on real face buttons, never the hat: a sideways body has no d-pad.
    private val LEFT_REMAP = mapOf(
        JoyconButton.Right.id to JoyconButton.X.id,
        JoyconButton.Down.id to JoyconButton.A.id,
        JoyconButton.Left.id to JoyconButton.B.id,
        JoyconButton.Up.id to JoyconButton.Y.id,
        JoyconButton.SlLeft.id to JoyconButton.R.id,
        JoyconButton.SrLeft.id to JoyconButton.ZR.id,
    )

    // Stick click becomes LS, the stick now being the left one.
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
