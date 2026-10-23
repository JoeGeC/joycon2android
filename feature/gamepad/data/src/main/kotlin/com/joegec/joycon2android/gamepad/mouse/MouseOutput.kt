package com.joegec.joycon2android.gamepad.mouse

import android.content.Context
import com.joegec.joycon2android.gamepad.UhidOutput
import com.joegec.joycon2android.gamepad.UhidRelay
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

class MouseOutput(
    scope: CoroutineScope,
    context: Context,
    acquireShell: (onResult: (PrivilegedShell?) -> Unit) -> Unit,
) : MouseRepository {

    private val pending = PendingMouseFrame()
    private val wake = Channel<Unit>(Channel.CONFLATED)
    private val output = UhidOutput(scope, context, acquireShell, MouseDescriptor.device, ::write)

    override val enabled: StateFlow<Boolean> = output.enabled

    override fun enable() = output.enable()

    override fun disable() = output.disable()

    override fun push(frame: MouseFrame) {
        if (enabled.value && pending.add(frame)) wake.trySend(Unit)
    }

    private suspend fun write(relay: UhidRelay) {
        pending.clear()
        for (signal in wake) pending.take()?.let { relay.sendReport(MouseReport.build(it)) }
    }
}
