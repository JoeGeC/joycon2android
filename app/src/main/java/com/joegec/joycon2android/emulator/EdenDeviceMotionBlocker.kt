package com.joegec.joycon2android.emulator

import android.os.Build
import android.util.Log
import com.joegec.joycon2android.dsu.motion.DeviceMotionBlocker
import com.joegec.joycon2android.emulatorconfig.EdenPaths
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EdenDeviceMotionBlocker(
    private val readyShell: () -> PrivilegedShell?,
) : DeviceMotionBlocker {

    override suspend fun setBlocked(blocked: Boolean) = withContext(Dispatchers.IO) {
        // Idle-uid sensor restriction and its shell override arrived in Android 9.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return@withContext
        val shell = readyShell() ?: return@withContext
        val command = if (blocked) "set-uid-state \"\$p\" idle" else "reset-uid-state \"\$p\""
        val packages = EdenPaths.PACKAGES.joinToString(" ") { "'$it'" }
        val proc = shell.shell("for p in $packages; do cmd sensorservice $command 2>/dev/null; done") ?: return@withContext
        try {
            proc.waitFor()
            Log.i(TAG, "Eden device motion blocked=$blocked")
        } catch (e: Exception) {
            Log.w(TAG, "Could not set Eden device motion blocked=$blocked", e)
        } finally {
            proc.destroy()
        }
    }

    private companion object {
        const val TAG = "EdenDeviceMotionBlocker"
    }
}
