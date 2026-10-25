package com.joegec.joycon2android.gamepad.mouse

import kotlinx.coroutines.flow.StateFlow

interface MouseRepository {
    val enabled: StateFlow<Boolean>

    fun enable()
    fun disable()
    fun push(frame: MouseFrame)
}
