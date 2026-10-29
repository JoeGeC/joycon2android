package com.joegec.joycon2android.gamepad

/** Linux input-event codes, from `linux/input-event-codes.h`. */
internal object Evdev {
    const val BUS_USB = 0x03

    const val EV_KEY = 0x01
    const val EV_ABS = 0x03

    const val KEY_BACK = 158
    const val KEY_HOMEPAGE = 172

    const val BTN_0 = 0x100
    const val BTN_1 = 0x101
    const val BTN_A = 0x130
    const val BTN_B = 0x131
    const val BTN_C = 0x132
    const val BTN_X = 0x133
    const val BTN_Y = 0x134
    const val BTN_Z = 0x135
    const val BTN_TL = 0x136
    const val BTN_TR = 0x137
    const val BTN_TL2 = 0x138
    const val BTN_TR2 = 0x139
    const val BTN_SELECT = 0x13A
    const val BTN_START = 0x13B
    const val BTN_MODE = 0x13C
    const val BTN_THUMBL = 0x13D
    const val BTN_THUMBR = 0x13E

    const val ABS_X = 0x00
    const val ABS_Y = 0x01
    const val ABS_Z = 0x02
    const val ABS_RZ = 0x05
    const val ABS_GAS = 0x09
    const val ABS_BRAKE = 0x0A
    const val ABS_HAT0X = 0x10
    const val ABS_HAT0Y = 0x11
}
