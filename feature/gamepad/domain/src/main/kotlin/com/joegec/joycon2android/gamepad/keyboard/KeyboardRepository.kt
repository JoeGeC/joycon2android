package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.KeyboardKey
import kotlinx.coroutines.flow.StateFlow

interface KeyboardRepository {
    val enabled: StateFlow<Boolean>

    fun enable()
    fun disable()

    /** The keys any binding can press, so the device declares what it needs and no more. */
    fun declare(bound: Set<KeyboardKey>)
    fun push(frame: KeyboardFrame)
}
