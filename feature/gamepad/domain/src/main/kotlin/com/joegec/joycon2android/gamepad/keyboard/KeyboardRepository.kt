package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.AndroidKey
import kotlinx.coroutines.flow.StateFlow

interface KeyboardRepository {
    val enabled: StateFlow<Boolean>

    fun enable()
    fun disable()
    fun push(held: Set<AndroidKey>)
}
