package com.joegec.joycon2android.gamepad

import kotlinx.coroutines.flow.StateFlow

/** Availability of the privileged backend (Shizuku) that the virtual gamepad depends on. */
interface PrivilegedAccessRepository {
    val shizukuAvailable: StateFlow<Boolean>
}
