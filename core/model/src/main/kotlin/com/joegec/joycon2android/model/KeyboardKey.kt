package com.joegec.joycon2android.model

enum class KeyboardKey {
    UP, DOWN, LEFT, RIGHT,
    ENTER, ESCAPE, SPACE, TAB, BACKSPACE, DELETE, PAGE_UP, PAGE_DOWN,
    LEFT_SHIFT, LEFT_CTRL, LEFT_ALT,
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
    DIGIT_1, DIGIT_2, DIGIT_3, DIGIT_4, DIGIT_5, DIGIT_6, DIGIT_7, DIGIT_8, DIGIT_9, DIGIT_0,
    F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12;

    val isLetter: Boolean get() = this in A..Z
}
