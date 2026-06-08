package com.joegec.joycon2android.domain

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side

/**
 * Infers Joy-Con side from observed input when BLE advertisement didn't identify it.
 *
 * Left-exclusive buttons: ZL, L, Minus, LS, DPad (Up/Down/Left/Right), Camera, SL(L), SR(L)
 * Right-exclusive buttons: ZR, R, Plus, RS, A, B, X, Y, Home, Chat, SL(R), SR(R)
 */
object SideInference {

    private val leftButtons = setOf(
        JoyconButton.ZL.id, JoyconButton.L.id, JoyconButton.Minus.id, JoyconButton.LS.id,
        JoyconButton.Up.id, JoyconButton.Down.id, JoyconButton.Left.id, JoyconButton.Right.id,
        JoyconButton.Camera.id, JoyconButton.SlLeft.id, JoyconButton.SrLeft.id,
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
