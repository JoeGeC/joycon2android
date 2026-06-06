package com.joegec.joycon2android.model

data class PlayerState(
    val player: PlayerNumber,
    val left: ConnectedJoycon? = null,
    val right: ConnectedJoycon? = null,
) {
    val hasController: Boolean get() = left != null || right != null
    val hasFullController: Boolean get() = left != null && right != null

    val pressed: Set<String>
        get() = (left?.input?.pressed ?: emptySet()) + (right?.input?.pressed ?: emptySet())

    val leftStickX: Int get() = left?.input?.stickX ?: 2048
    val leftStickY: Int get() = left?.input?.stickY ?: 2048
    val rightStickX: Int get() = right?.input?.stickX ?: 2048
    val rightStickY: Int get() = right?.input?.stickY ?: 2048

    val leftInput: JoyconInput get() = left?.input ?: JoyconInput()
    val rightInput: JoyconInput get() = right?.input ?: JoyconInput()
}
