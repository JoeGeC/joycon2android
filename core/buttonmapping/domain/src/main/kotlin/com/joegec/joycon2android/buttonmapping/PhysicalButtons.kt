package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.GameCubeButtons
import com.joegec.joycon2android.model.JoyconButton

/** Only what that side physically has, so every source can fire. */
fun JoyconSide.physicalButtons(): List<JoyconButton> = when (this) {
    JoyconSide.DUAL -> JoyconButton.entries
    JoyconSide.GAMECUBE -> GameCubeButtons.present.toList()
    JoyconSide.LEFT -> listOf(
        JoyconButton.L, JoyconButton.ZL, JoyconButton.Minus, JoyconButton.LS,
        JoyconButton.Up, JoyconButton.Down, JoyconButton.Left, JoyconButton.Right,
        JoyconButton.Capture, JoyconButton.SlLeft, JoyconButton.SrLeft,
    )
    JoyconSide.RIGHT -> listOf(
        JoyconButton.R, JoyconButton.ZR, JoyconButton.Plus, JoyconButton.RS,
        JoyconButton.A, JoyconButton.B, JoyconButton.X, JoyconButton.Y,
        JoyconButton.Home, JoyconButton.Chat, JoyconButton.SrRight, JoyconButton.SlRight,
    )
}
