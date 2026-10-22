package com.joegec.joycon2android.gamepad.mouse

import android.content.Context
import android.util.Log
import com.joegec.joycon2android.gamepad.UhidRelay
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MouseOutput(
    private val scope: CoroutineScope,
    private val context: Context,
    private val acquireShell: (onResult: (PrivilegedShell?) -> Unit) -> Unit,
) : MouseRepository {

    private val _enabled = MutableStateFlow(false)
    override val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val pending = PendingMouseFrame()
    private val wake = Channel<Unit>(Channel.CONFLATED)
    private var relay: UhidRelay? = null
    private var writer: Job? = null
    private var wanted = false

    @Synchronized
    override fun enable() {
        if (wanted) return
        wanted = true
        acquireShell { shell ->
            if (shell != null) scope.launch(Dispatchers.IO) { start(shell) } else synchronized(this) { wanted = false }
        }
    }

    @Synchronized
    override fun disable() {
        wanted = false
        _enabled.value = false
        writer?.cancel()
        writer = null
        val stopped = relay ?: return
        relay = null
        scope.launch(Dispatchers.IO) { stopped.destroy() }
    }

    override fun push(frame: MouseFrame) {
        if (_enabled.value && pending.add(frame)) wake.trySend(Unit)
    }

    private fun start(shell: PrivilegedShell) {
        val created = UhidRelay(MouseDescriptor.device)
        if (!created.create(context, shell)) {
            Log.e(TAG, "Failed to create virtual mouse")
            return
        }
        synchronized(this) {
            if (!wanted || relay != null) {
                scope.launch(Dispatchers.IO) { created.destroy() }
                return
            }
            relay = created
            pending.clear()
            writer = scope.launch(Dispatchers.IO) {
                for (signal in wake) pending.take()?.let { created.sendReport(MouseReport.build(it)) }
            }
            _enabled.value = true
        }
    }

    private companion object {
        const val TAG = "MouseOutput"
    }
}
