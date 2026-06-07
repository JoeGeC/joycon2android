package com.joegec.joycon2android.model

data class GamepadState(
    val pressed: Set<String>,
    val leftStickX: Int,
    val leftStickY: Int,
    val rightStickX: Int,
    val rightStickY: Int,
) {
    companion object {
        private const val CENTER = 2048

        fun from(state: PlayerState): GamepadState = when {
            state.hasFullController -> GamepadState(
                pressed = state.pressed,
                leftStickX = state.leftStickX,
                leftStickY = state.leftStickY,
                rightStickX = state.rightStickX,
                rightStickY = state.rightStickY,
            )
            state.left != null -> {
                val input = state.left.input
                val (sx, sy) = SidewaysMapper.rotateStickLeft(input.stickX, input.stickY)
                GamepadState(
                    pressed = SidewaysMapper.remapButtonsLeft(state.pressed),
                    leftStickX = sx,
                    leftStickY = sy,
                    rightStickX = CENTER,
                    rightStickY = CENTER,
                )
            }
            state.right != null -> {
                val input = state.right.input
                val (sx, sy) = SidewaysMapper.rotateStickRight(input.stickX, input.stickY)
                GamepadState(
                    pressed = SidewaysMapper.remapButtonsRight(state.pressed),
                    leftStickX = sx,
                    leftStickY = sy,
                    rightStickX = CENTER,
                    rightStickY = CENTER,
                )
            }
            else -> GamepadState(
                pressed = emptySet(),
                leftStickX = CENTER,
                leftStickY = CENTER,
                rightStickX = CENTER,
                rightStickY = CENTER,
            )
        }
    }
}
