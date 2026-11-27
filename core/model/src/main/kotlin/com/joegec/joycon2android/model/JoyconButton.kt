package com.joegec.joycon2android.model

enum class JoyconButton(val id: String, val label: String) {
    ZL("ZL", "ZL"),
    L("L", "L"),
    Minus("-", "-"),
    LS("LS", "LS"),
    Down("Down", "Down"),
    Up("Up", "Up"),
    Right("Right", "Right"),
    Left("Left", "Left"),
    Capture("Capture", "Capture"),
    SrLeft("SR(L)", "SR"),
    SlLeft("SL(L)", "SL"),
    Home("Home", "Home"),
    Chat("Chat", "C"),
    Plus("+", "+"),
    SrRight("SR(R)", "SR"),
    SlRight("SL(R)", "SL"),
    R("R", "R"),
    ZR("ZR", "ZR"),
    RS("RS", "RS"),
    Y("Y", "Y"),
    X("X", "X"),
    B("B", "B"),
    A("A", "A"),
    GL("GL", "GL"),
    GR("GR", "GR"),
    ;

    companion object {
        val leftJoycon: Set<JoyconButton> = setOf(ZL, L, Minus, LS, Up, Down, Left, Right, Capture, SlLeft, SrLeft)
        val rightJoycon: Set<JoyconButton> = setOf(ZR, R, Plus, RS, A, B, X, Y, Home, Chat, SlRight, SrRight)
    }
}
