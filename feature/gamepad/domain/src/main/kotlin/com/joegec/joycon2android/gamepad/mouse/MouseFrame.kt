package com.joegec.joycon2android.gamepad.mouse

/** Movement in sensor counts, wheel in detents (+ scrolls up). */
data class MouseFrame(
    val dx: Int = 0,
    val dy: Int = 0,
    val wheel: Int = 0,
    val buttons: Set<MouseButton> = emptySet(),
) {
    val moves: Boolean get() = dx != 0 || dy != 0 || wheel != 0

    operator fun plus(other: MouseFrame) =
        MouseFrame(dx + other.dx, dy + other.dy, wheel + other.wheel, buttons + other.buttons)
}
