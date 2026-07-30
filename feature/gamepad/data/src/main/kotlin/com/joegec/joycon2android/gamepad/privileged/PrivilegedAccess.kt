package com.joegec.joycon2android.gamepad.privileged

import android.util.Log
import com.joegec.joycon2android.gamepad.PrivilegedAccessRepository
import com.joegec.joycon2android.gamepad.shizuku.ShizukuPermissionHandler
import com.joegec.joycon2android.gamepad.shizuku.ShizukuShell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

/**
 * Grants the virtual gamepad and emulator-config writes shell-uid access to `/dev/uhid`
 * and app config files through Shizuku, the app's only privileged backend.
 */
class PrivilegedAccess : PrivilegedAccessRepository {

    private val shizuku = ShizukuShell()

    // Shizuku's binder can arrive or die after launch (service started/stopped, app updated),
    // so track it live rather than sampling once. Sticky delivers the current state on register.
    private val _shizukuAvailable = MutableStateFlow(ShizukuPermissionHandler.isShizukuAvailable)
    override val shizukuAvailable: StateFlow<Boolean> = _shizukuAvailable.asStateFlow()

    init {
        Shizuku.addBinderReceivedListenerSticky { _shizukuAvailable.value = true }
        Shizuku.addBinderDeadListener { _shizukuAvailable.value = false }
    }

    fun acquire(onResult: (PrivilegedShell?) -> Unit) {
        when {
            shizuku.isReady -> {
                Log.i(TAG, "acquire: shizuku ready")
                onResult(shizuku)
            }
            ShizukuPermissionHandler.isShizukuAvailable -> {
                Log.i(TAG, "acquire: requesting shizuku permission")
                ShizukuPermissionHandler.requestPermission { granted ->
                    Log.i(TAG, "acquire: shizuku permission granted=$granted")
                    onResult(if (granted) shizuku else null)
                }
            }
            else -> {
                Log.i(TAG, "acquire: shizuku unavailable")
                onResult(null)
            }
        }
    }

    private companion object {
        const val TAG = "PrivilegedAccess"
    }
}
