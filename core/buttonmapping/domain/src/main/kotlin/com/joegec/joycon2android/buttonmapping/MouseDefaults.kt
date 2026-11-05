package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.MouseAction

/** Under every layout, so the Mouse rows and Toggle combo start bound: docs/virtual-gamepad.md#mouse */
internal fun mouseDefaults(side: JoyconSide): Map<String, String> {
    val left = side == JoyconSide.LEFT
    fun button(leftButton: JoyconButton, rightButton: JoyconButton) = MappingSource.Button(if (left) leftButton else rightButton).id
    val stick = if (left) StickSource.LEFT_STICK else StickSource.RIGHT_STICK
    return mapOf(
        MouseAction.LEFT_CLICK.targetKey to button(JoyconButton.L, JoyconButton.R),
        MouseAction.RIGHT_CLICK.targetKey to button(JoyconButton.ZL, JoyconButton.ZR),
        MouseAction.MIDDLE_CLICK.targetKey to button(JoyconButton.LS, JoyconButton.RS),
        MouseAction.SCROLL_UP.targetKey to MappingSource.Stick(stick, StickDirection.UP).id,
        MouseAction.SCROLL_DOWN.targetKey to MappingSource.Stick(stick, StickDirection.DOWN).id,
        MOUSE_MODE_KEY to button(JoyconButton.Capture, JoyconButton.Chat),
    )
}
