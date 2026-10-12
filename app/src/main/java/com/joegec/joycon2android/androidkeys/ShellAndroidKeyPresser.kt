package com.joegec.joycon2android.androidkeys

import android.util.Log
import android.view.KeyEvent
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import com.joegec.joycon2android.model.AndroidKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Injects a key as if pressed on the device. Why not for every key: docs/virtual-gamepad.md#android-keys */
class ShellAndroidKeyPresser(
    private val scope: CoroutineScope,
    private val readyShell: () -> PrivilegedShell?,
) {

    fun press(key: AndroidKey) {
        val shell = readyShell() ?: return
        scope.launch(Dispatchers.IO) {
            val proc = shell.shell("input keyevent ${key.keyCode}") ?: return@launch
            try {
                proc.waitFor()
            } catch (e: Exception) {
                Log.w(TAG, "Could not press $key", e)
            } finally {
                proc.destroy()
            }
        }
    }

    private val AndroidKey.keyCode: Int
        get() = when (this) {
            AndroidKey.HOME -> KeyEvent.KEYCODE_HOME
            AndroidKey.BACK -> KeyEvent.KEYCODE_BACK
            AndroidKey.SCREENSHOT -> KeyEvent.KEYCODE_SYSRQ
        }

    private companion object {
        const val TAG = "ShellAndroidKeyPresser"
    }
}
