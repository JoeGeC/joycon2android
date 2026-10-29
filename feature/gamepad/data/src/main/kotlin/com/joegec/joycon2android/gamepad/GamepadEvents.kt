package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.gamepad.Evdev.EV_ABS
import com.joegec.joycon2android.gamepad.Evdev.EV_KEY
import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.JoyconButton

/** A frame as the pad's whole state. Linux drops a repeated value, so only changes reach Android. */
object GamepadEvents {

    // Each code's keycode carries the button's own name: docs/virtual-gamepad.md#buttons-and-keycodes
    private val BUTTON_CODES: Map<String, Int> = mapOf(
        JoyconButton.A.id to Evdev.BTN_A,
        JoyconButton.B.id to Evdev.BTN_B,
        JoyconButton.Capture.id to Evdev.BTN_C,
        JoyconButton.X.id to Evdev.BTN_X,
        JoyconButton.Y.id to Evdev.BTN_Y,
        JoyconButton.GL.id to Evdev.BTN_Z,
        JoyconButton.L.id to Evdev.BTN_TL,
        JoyconButton.R.id to Evdev.BTN_TR,
        JoyconButton.ZL.id to Evdev.BTN_TL2,
        JoyconButton.ZR.id to Evdev.BTN_TR2,
        JoyconButton.Minus.id to Evdev.BTN_SELECT,
        JoyconButton.Plus.id to Evdev.BTN_START,
        JoyconButton.Home.id to Evdev.BTN_MODE,
        JoyconButton.LS.id to Evdev.BTN_THUMBL,
        JoyconButton.RS.id to Evdev.BTN_THUMBR,
        JoyconButton.GR.id to Evdev.BTN_0,
        JoyconButton.Chat.id to Evdev.BTN_1,
    )

    // docs/virtual-gamepad.md#android-keys
    internal val ANDROID_KEY_CODES: Map<AndroidKey, Int> = mapOf(
        AndroidKey.HOME to Evdev.KEY_HOMEPAGE,
        AndroidKey.BACK to Evdev.KEY_BACK,
    )

    val KEYS: List<Int> = BUTTON_CODES.values + ANDROID_KEY_CODES.values

    private const val STICK_CENTER = 2048
    private const val STICK_MAX = 32767

    fun of(frame: GamepadFrame): List<InputEvent> {
        val gamepad = frame.state.gamepad
        val pressed = gamepad.pressed
        val (hatX, hatY) = hat(pressed)
        return BUTTON_CODES.map { (id, code) -> key(code, id in pressed) } +
            ANDROID_KEY_CODES.map { (key, code) -> key(code, key in frame.androidKeys) } +
            listOf(
                abs(Evdev.ABS_HAT0X, hatX),
                abs(Evdev.ABS_HAT0Y, hatY),
                abs(Evdev.ABS_X, stick(gamepad.leftStickX)),
                abs(Evdev.ABS_Y, stick(4096 - gamepad.leftStickY)),
                abs(Evdev.ABS_Z, stick(gamepad.rightStickX)),
                abs(Evdev.ABS_RZ, stick(4096 - gamepad.rightStickY)),
                abs(Evdev.ABS_BRAKE, gamepad.leftTrigger),
                abs(Evdev.ABS_GAS, gamepad.rightTrigger),
            )
    }

    private fun key(code: Int, down: Boolean) = InputEvent(EV_KEY, code, if (down) 1 else 0)

    private fun abs(code: Int, value: Int) = InputEvent(EV_ABS, code, value)

    // 0–4095, centre 2048, to ±32767; Y arrives inverted, up negative.
    private fun stick(value: Int): Int =
        ((value - STICK_CENTER).toLong() * STICK_MAX / STICK_CENTER).toInt().coerceIn(-STICK_MAX, STICK_MAX)

    // One direction wins a conflict, as the HID hat's single value did. Up is −1.
    private fun hat(pressed: Set<String>): Pair<Int, Int> {
        val up = JoyconButton.Up.id in pressed
        val down = JoyconButton.Down.id in pressed
        val left = JoyconButton.Left.id in pressed
        val right = JoyconButton.Right.id in pressed
        return when {
            up && right -> 1 to -1
            right && down -> 1 to 1
            down && left -> -1 to 1
            left && up -> -1 to -1
            up -> 0 to -1
            right -> 1 to 0
            down -> 0 to 1
            left -> -1 to 0
            else -> 0 to 0
        }
    }
}
