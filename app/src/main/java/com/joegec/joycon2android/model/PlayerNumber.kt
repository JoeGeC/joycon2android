package com.joegec.joycon2android.model

enum class PlayerNumber(val index: Int, val ledBitmask: Byte) {
    P1(1, 0x01),
    P2(2, 0x02),
    P3(3, 0x04),
    P4(4, 0x08),
    P5(5, 0x09),  // LEDs 1 + 4
    P6(6, 0x05),  // LEDs 1 + 3
    P7(7, 0x0D),  // LEDs 1 + 3 + 4
    P8(8, 0x06),  // LEDs 2 + 3
}
