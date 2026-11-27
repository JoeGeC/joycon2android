package com.joegec.joycon2android.assignment

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side

/** For a Joy-Con whose advertisement didn't reveal its side. */
object SideInference {

    private val leftButtons = JoyconButton.leftJoycon.map { it.id }.toSet()
    private val rightButtons = JoyconButton.rightJoycon.map { it.id }.toSet()

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
