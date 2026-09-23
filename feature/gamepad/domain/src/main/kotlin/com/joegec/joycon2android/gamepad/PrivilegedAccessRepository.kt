package com.joegec.joycon2android.gamepad

import kotlinx.coroutines.flow.StateFlow

interface PrivilegedAccessRepository {
    val shizukuAvailable: StateFlow<Boolean>
}
