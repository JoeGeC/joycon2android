package com.joegec.joycon2android.assignment

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side

/** For a Joy-Con whose advertisement didn't reveal its side. */
object SideInference {

    private val leftButtons = setOf(
        JoyconButton.ZL.id, JoyconButton.L.id, JoyconButton.Minus.id, JoyconButton.LS.id,
        JoyconButton.Up.id, JoyconButton.Down.id, JoyconButton.Left.id, JoyconButton.Right.id,
        JoyconButton.Capture.id, JoyconButton.SlLeft.id, JoyconButton.SrLeft.id,
    )

    private val rightButtons = setOf(
        JoyconButton.ZR.id, JoyconButton.R.id, JoyconButton.Plus.id, JoyconButton.RS.id,
        JoyconButton.A.id, JoyconButton.B.id, JoyconButton.X.id, JoyconButton.Y.id,
        JoyconButton.Home.id, JoyconButton.Chat.id, JoyconButton.SlRight.id, JoyconButton.SrRight.id,
    )

    fun inferSide(input: JoyconInput): Side {
        val hasLeft = input.pressed.any { it in leftButtons }
        val hasRight = input.pressed.any { it in rightButtons }
        return when {
            hasLeft && !hasRight -> Side.LEFT
            hasRight && !hasLeft -> Side.RIGHT
            else -> Side.UNKNOWN
        }
    }
}
