package com.joegec.joycon2android.model

data class PlayerState(
    val player: PlayerNumber,
    val left: ConnectedJoycon? = null,
    val right: ConnectedJoycon? = null,
    /** docs/virtual-gamepad.md#single-joy-con */
    val asSingleJoycon: Boolean = false,
) {
    val hasPro: Boolean get() = left != null && left.side == Side.PRO
    val hasController: Boolean get() = left != null || right != null
    val hasFullController: Boolean get() = left != null && right != null
    val isLone: Boolean get() = hasController && !hasFullController && !hasPro
    val isSideways: Boolean get() = isLone && !asSingleJoycon
    val isSingleJoycon: Boolean get() = isLone && asSingleJoycon
    val hasAnalogTriggers: Boolean get() = hasPro && left!!.model.hasAnalogTriggers
    val hasGameCube: Boolean get() = hasPro && left!!.model == ControllerModel.GAMECUBE

    // As the hardware reports it, before any sideways rotation or rail remap ([gamepad]).
    val pressed: Set<String>
        get() = if (hasPro) left!!.input.pressed
                else (left?.input?.pressed ?: emptySet()) + (right?.input?.pressed ?: emptySet())

    val leftStickX: Int get() = left?.input?.stickX ?: 2048
    val leftStickY: Int get() = left?.input?.stickY ?: 2048
    val rightStickX: Int get() = if (hasPro) left!!.input.rightStickX else right?.input?.stickX ?: 2048
    val rightStickY: Int get() = if (hasPro) left!!.input.rightStickY else right?.input?.stickY ?: 2048

    val leftInput: JoyconInput get() = left?.input ?: JoyconInput()
    val rightInput: JoyconInput get() = right?.input ?: JoyconInput()

    // A pair's right Joy-Con is the Wii Remote hand.
    val motionSource: ConnectedJoycon? get() = right ?: left

    val gamepad: GamepadState get() = GamepadState.from(this)

    /** Equal from one packet to the next, for screens that show who holds what but no input. */
    fun withoutInput(): PlayerState =
        copy(left = left?.copy(input = JoyconInput()), right = right?.copy(input = JoyconInput()))
}
