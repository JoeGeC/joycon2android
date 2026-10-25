package com.joegec.joycon2android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** Keeps a shared UHID device, the mouse or keyboard, up while its setting is on. */
class PrivilegedDevicePolicy(
    private val scope: CoroutineScope,
    private val setting: Flow<Boolean>,
    private val privilegedShellAvailable: Flow<Boolean>,
    private val setEnabled: (Boolean) -> Unit,
) {

    fun start() {
        scope.launch {
            // The relay dies with the shell, so the device goes down and comes back with it.
            combine(setting, privilegedShellAvailable) { wanted, shellUp -> wanted && shellUp }
                .distinctUntilChanged()
                .collect { setEnabled(it) }
        }
    }
}
