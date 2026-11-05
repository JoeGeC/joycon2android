package com.joegec.joycon2android.buttonmapping.target

import com.joegec.joycon2android.model.JoyconButton

/** The virtual gamepad's own buttons, each sent on [slot]'s code: docs/virtual-gamepad.md#buttons-and-keycodes */
enum class PadButton(val slot: JoyconButton) {
    A(JoyconButton.A),
    B(JoyconButton.B),
    X(JoyconButton.X),
    Y(JoyconButton.Y),
    L(JoyconButton.L),
    R(JoyconButton.R),
    ZL(JoyconButton.ZL),
    ZR(JoyconButton.ZR),
    Plus(JoyconButton.Plus),
    Minus(JoyconButton.Minus),
    Home(JoyconButton.Home),
    Capture(JoyconButton.Capture),
    LStickClick(JoyconButton.LS),
    RStickClick(JoyconButton.RS),
    DPadUp(JoyconButton.Up),
    DPadDown(JoyconButton.Down),
    DPadLeft(JoyconButton.Left),
    DPadRight(JoyconButton.Right),
    GL(JoyconButton.GL),
    GR(JoyconButton.GR),
    C(JoyconButton.Chat),
}
