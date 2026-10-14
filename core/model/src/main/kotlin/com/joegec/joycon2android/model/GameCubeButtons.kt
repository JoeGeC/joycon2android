package com.joegec.joycon2android.model

// The NSO GameCube controller has no −, stick clicks or back paddles.
object GameCubeButtons {
    val present: Set<JoyconButton> = setOf(
        JoyconButton.A, JoyconButton.B, JoyconButton.X, JoyconButton.Y,
        JoyconButton.L, JoyconButton.R, JoyconButton.ZL, JoyconButton.ZR, JoyconButton.Plus,
        JoyconButton.Up, JoyconButton.Down, JoyconButton.Left, JoyconButton.Right,
        JoyconButton.Home, JoyconButton.Capture, JoyconButton.Chat,
    )

    /** Null where the controller marks the button as the Switch 2 controllers do. Its Z reports as ZR. */
    fun marking(button: JoyconButton): String? = if (button == JoyconButton.ZR) "Z" else null
}
