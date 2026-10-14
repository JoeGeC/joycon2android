package com.joegec.joycon2android.model

data class GamepadState(
    val pressed: Set<String>,
    val leftStickX: Int,
    val leftStickY: Int,
    val rightStickX: Int,
    val rightStickY: Int,
    val leftTrigger: Int = 0,
    val rightTrigger: Int = 0,
) {
    companion object {
        private const val CENTER = 2048
        private const val TRIGGER_PULLED = 255

        fun from(state: PlayerState): GamepadState = buttonsAndSticks(state).withTriggers(state)

        private fun GamepadState.withTriggers(state: PlayerState): GamepadState =
            if (state.hasAnalogTriggers) {
                copy(
                    pressed = AnalogTriggerMapper.remapButtons(pressed),
                    leftTrigger = state.leftInput.triggerLeft,
                    rightTrigger = state.leftInput.triggerRight,
                )
            } else {
                copy(leftTrigger = pulledBy(JoyconButton.ZL), rightTrigger = pulledBy(JoyconButton.ZR))
            }

        private fun GamepadState.pulledBy(button: JoyconButton): Int = if (button.id in pressed) TRIGGER_PULLED else 0

        private fun buttonsAndSticks(state: PlayerState): GamepadState = when {
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
