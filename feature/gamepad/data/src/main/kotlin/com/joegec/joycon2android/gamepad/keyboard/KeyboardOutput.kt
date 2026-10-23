package com.joegec.joycon2android.gamepad.keyboard

import android.content.Context
import com.joegec.joycon2android.gamepad.UhidOutput
import com.joegec.joycon2android.gamepad.UhidRelay
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import com.joegec.joycon2android.model.KeyboardKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Up as whichever of its two devices the bound keys need: docs/virtual-gamepad.md#keyboard */
class KeyboardOutput(
    scope: CoroutineScope,
    context: Context,
    acquireShell: (onResult: (PrivilegedShell?) -> Unit) -> Unit,
) : KeyboardRepository {

    private val frame = MutableStateFlow(KeyboardFrame())
    private val withLetters = UhidOutput(scope, context, acquireShell, KeyboardDescriptor.withLetters, ::write)
    private val withoutLetters = UhidOutput(scope, context, acquireShell, KeyboardDescriptor.withoutLetters, ::write)
    private var letters = false
    private var wanted = false

    override val enabled: StateFlow<Boolean> =
        combine(withLetters.enabled, withoutLetters.enabled) { a, b -> a || b }
            .stateIn(scope, SharingStarted.Eagerly, false)

    @Synchronized
    override fun enable() {
        wanted = true
        current().enable()
    }

    @Synchronized
    override fun disable() {
        wanted = false
        withLetters.disable()
        withoutLetters.disable()
        frame.value = KeyboardFrame()
    }

    @Synchronized
    override fun declare(bound: Set<KeyboardKey>) {
        val needsLetters = bound.any { it.isLetter }
        if (needsLetters == letters) return
        val previous = current()
        letters = needsLetters
        if (!wanted) return
        previous.disable()
        current().enable()
    }

    override fun push(frame: KeyboardFrame) {
        if (enabled.value) this.frame.value = frame
    }

    private fun current() = if (letters) withLetters else withoutLetters

    private suspend fun write(relay: UhidRelay) {
        frame.collect { held -> KeyboardReport.build(held).forEach(relay::sendReport) }
    }
}
