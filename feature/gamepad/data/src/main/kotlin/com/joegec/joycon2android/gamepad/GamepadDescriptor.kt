package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.gamepad.Evdev.ABS_BRAKE
import com.joegec.joycon2android.gamepad.Evdev.ABS_GAS
import com.joegec.joycon2android.gamepad.Evdev.ABS_HAT0X
import com.joegec.joycon2android.gamepad.Evdev.ABS_HAT0Y
import com.joegec.joycon2android.gamepad.Evdev.ABS_RZ
import com.joegec.joycon2android.gamepad.Evdev.ABS_X
import com.joegec.joycon2android.gamepad.Evdev.ABS_Y
import com.joegec.joycon2android.gamepad.Evdev.ABS_Z

object GamepadDescriptor {

    // Not Nintendo's: docs/virtual-gamepad.md#uinput
    private const val VENDOR_ID = 0x1234
    private const val PRODUCT_ID = 0x5678
    private const val VERSION = 1
    private const val RUMBLE_EFFECTS = 16

    private const val STICK_MAX = 32767
    private const val TRIGGER_MAX = 255

    fun device(playerIndex: Int) = UinputDevice(
        name = "Joy-Con Virtual Gamepad $playerIndex",
        phys = "joycon2android/$playerIndex",
        vendorId = VENDOR_ID,
        productId = PRODUCT_ID,
        version = VERSION,
        keys = GamepadEvents.KEYS,
        axes = AXES,
        rumbleEffects = RUMBLE_EFFECTS,
    )

    // What Linux's HID layer gave the HID pad these axes: docs/virtual-gamepad.md#axes
    private val AXES = listOf(
        stick(ABS_X),
        stick(ABS_Y),
        stick(ABS_Z),
        stick(ABS_RZ, resolution = 11921),
        trigger(ABS_BRAKE),
        trigger(ABS_GAS),
        AbsAxis(ABS_HAT0X, -1, 1),
        AbsAxis(ABS_HAT0Y, -1, 1),
    )

    private fun stick(code: Int, resolution: Int = 0) =
        AbsAxis(code, -STICK_MAX, STICK_MAX, fuzz = 2 * STICK_MAX shr 8, flat = 2 * STICK_MAX shr 4, resolution = resolution)

    private fun trigger(code: Int) = AbsAxis(code, 0, TRIGGER_MAX, fuzz = TRIGGER_MAX shr 8, flat = TRIGGER_MAX shr 4)
}
