package com.joegec.joycon2android.gamepad

import android.content.Context
import android.util.Log
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One UHID device shared by every controller, up while wanted and a shell is granted. */
class UhidOutput(
    private val scope: CoroutineScope,
    private val context: Context,
    private val acquireShell: (onResult: (PrivilegedShell?) -> Unit) -> Unit,
    private val device: UhidDevice,
    private val write: suspend (UhidRelay) -> Unit,
) {

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private var relay: UhidRelay? = null
    private var writer: Job? = null
    private var wanted = false

    @Synchronized
    fun enable() {
        if (wanted) return
        wanted = true
        acquireShell { shell ->
            if (shell != null) scope.launch(Dispatchers.IO) { start(shell) } else synchronized(this) { wanted = false }
        }
    }

    @Synchronized
    fun disable() {
        wanted = false
        _enabled.value = false
        writer?.cancel()
        writer = null
        val stopped = relay ?: return
        relay = null
        scope.launch(Dispatchers.IO) { stopped.destroy() }
    }

    private fun start(shell: PrivilegedShell) {
        val created = UhidRelay(device)
        if (!created.create(context, shell)) {
            Log.e(TAG, "Failed to create ${device.name}")
            return
        }
        synchronized(this) {
            if (!wanted || relay != null) {
                scope.launch(Dispatchers.IO) { created.destroy() }
                return
            }
            relay = created
            writer = scope.launch(Dispatchers.IO) { write(created) }
            _enabled.value = true
        }
    }

    private companion object {
        const val TAG = "UhidOutput"
    }
}
