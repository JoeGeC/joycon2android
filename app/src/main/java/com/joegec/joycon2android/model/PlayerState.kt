package com.joegec.joycon2android.model

data class PlayerState(
    val player: PlayerNumber,
    val left: ConnectedJoycon? = null,
    val right: ConnectedJoycon? = null,
) {
    val hasController: Boolean get() = left != null || right != null
    val hasFullController: Boolean get() = left != null && right != null
    val isSideways: Boolean get() = hasController && !hasFullController

    // Raw hardware button state (for UI display showing physical button activity)
    val pressed: Set<String>
        get() = (left?.input?.pressed ?: emptySet()) + (right?.input?.pressed ?: emptySet())

    val leftStickX: Int get() = left?.input?.stickX ?: 2048
    val leftStickY: Int get() = left?.input?.stickY ?: 2048
    val rightStickX: Int get() = right?.input?.stickX ?: 2048
    val rightStickY: Int get() = right?.input?.stickY ?: 2048

    val leftInput: JoyconInput get() = left?.input ?: JoyconInput()
    val rightInput: JoyconInput get() = right?.input ?: JoyconInput()

    // Gamepad-oriented state (rotated sticks + remapped buttons for HID output and consumers
    // that want standard gamepad semantics regardless of physical orientation)
    val gamepad: GamepadState get() = GamepadState.from(this)
}
