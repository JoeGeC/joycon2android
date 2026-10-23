package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.KeyboardKey

/** HID Keyboard page usages (HUT 1.4, §10). A modifier is a bit in the report's first byte instead. */
object KeyboardUsages {

    const val PRINT_SCREEN = 0x46

    // Past the letters, which end at Z.
    const val FIRST_NON_LETTER = 0x1E

    fun modifierBit(key: KeyboardKey): Int? = when (key) {
        KeyboardKey.LEFT_CTRL -> 0
        KeyboardKey.LEFT_SHIFT -> 1
        KeyboardKey.LEFT_ALT -> 2
        else -> null
    }

    fun usage(key: KeyboardKey): Int? = when (key) {
        KeyboardKey.A -> 0x04
        KeyboardKey.B -> 0x05
        KeyboardKey.C -> 0x06
        KeyboardKey.D -> 0x07
        KeyboardKey.E -> 0x08
        KeyboardKey.F -> 0x09
        KeyboardKey.G -> 0x0A
        KeyboardKey.H -> 0x0B
        KeyboardKey.I -> 0x0C
        KeyboardKey.J -> 0x0D
        KeyboardKey.K -> 0x0E
        KeyboardKey.L -> 0x0F
        KeyboardKey.M -> 0x10
        KeyboardKey.N -> 0x11
        KeyboardKey.O -> 0x12
        KeyboardKey.P -> 0x13
        KeyboardKey.Q -> 0x14
        KeyboardKey.R -> 0x15
        KeyboardKey.S -> 0x16
        KeyboardKey.T -> 0x17
        KeyboardKey.U -> 0x18
        KeyboardKey.V -> 0x19
        KeyboardKey.W -> 0x1A
        KeyboardKey.X -> 0x1B
        KeyboardKey.Y -> 0x1C
        KeyboardKey.Z -> 0x1D
        KeyboardKey.DIGIT_1 -> 0x1E
        KeyboardKey.DIGIT_2 -> 0x1F
        KeyboardKey.DIGIT_3 -> 0x20
        KeyboardKey.DIGIT_4 -> 0x21
        KeyboardKey.DIGIT_5 -> 0x22
        KeyboardKey.DIGIT_6 -> 0x23
        KeyboardKey.DIGIT_7 -> 0x24
        KeyboardKey.DIGIT_8 -> 0x25
        KeyboardKey.DIGIT_9 -> 0x26
        KeyboardKey.DIGIT_0 -> 0x27
        KeyboardKey.ENTER -> 0x28
        KeyboardKey.ESCAPE -> 0x29
        KeyboardKey.BACKSPACE -> 0x2A
        KeyboardKey.TAB -> 0x2B
        KeyboardKey.SPACE -> 0x2C
        KeyboardKey.F1 -> 0x3A
        KeyboardKey.F2 -> 0x3B
        KeyboardKey.F3 -> 0x3C
        KeyboardKey.F4 -> 0x3D
        KeyboardKey.F5 -> 0x3E
        KeyboardKey.F6 -> 0x3F
        KeyboardKey.F7 -> 0x40
        KeyboardKey.F8 -> 0x41
        KeyboardKey.F9 -> 0x42
        KeyboardKey.F10 -> 0x43
        KeyboardKey.F11 -> 0x44
        KeyboardKey.F12 -> 0x45
        KeyboardKey.PAGE_UP -> 0x4B
        KeyboardKey.DELETE -> 0x4C
        KeyboardKey.PAGE_DOWN -> 0x4E
        KeyboardKey.RIGHT -> 0x4F
        KeyboardKey.LEFT -> 0x50
        KeyboardKey.DOWN -> 0x51
        KeyboardKey.UP -> 0x52
        KeyboardKey.LEFT_SHIFT, KeyboardKey.LEFT_CTRL, KeyboardKey.LEFT_ALT -> null
    }
}
