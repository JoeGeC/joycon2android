package com.joegec.joycon2android.model

enum class PlayerNumber(val index: Int, val ledBitmask: Byte) {
    P1(1, 0x01),
    P2(2, 0x02),
    P3(3, 0x04),
    P4(4, 0x08),
}
