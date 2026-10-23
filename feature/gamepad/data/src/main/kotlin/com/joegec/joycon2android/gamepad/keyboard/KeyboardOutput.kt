package com.joegec.joycon2android.gamepad.keyboard

import android.content.Context
import com.joegec.joycon2android.gamepad.UhidOutput
import com.joegec.joycon2android.gamepad.UhidRelay
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import com.joegec.joycon2android.model.AndroidKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class KeyboardOutput(
    scope: CoroutineScope,
    context: Context,
    acquireShell: (onResult: (PrivilegedShell?) -> Unit) -> Unit,
) : KeyboardRepository {

    private val held = MutableStateFlow(emptySet<AndroidKey>())
    private val output = UhidOutput(scope, context, acquireShell, KeyboardDescriptor.device, ::write)

    override val enabled: StateFlow<Boolean> = output.enabled

    override fun enable() = output.enable()

    override fun disable() {
        output.disable()
        held.value = emptySet()
    }

    override fun push(held: Set<AndroidKey>) {
        if (enabled.value) this.held.value = held
    }

    private suspend fun write(relay: UhidRelay) {
        held.collect { keys -> KeyboardReport.build(keys).forEach(relay::sendReport) }
    }
}
