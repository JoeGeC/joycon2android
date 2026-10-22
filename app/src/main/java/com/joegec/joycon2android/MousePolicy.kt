package com.joegec.joycon2android

import com.joegec.joycon2android.gamepad.mouse.SetMouseEnabledUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** docs/virtual-gamepad.md#mouse */
class MousePolicy(
    private val scope: CoroutineScope,
    private val mouseSetting: Flow<Boolean>,
    private val privilegedShellAvailable: Flow<Boolean>,
    private val setMouseEnabled: SetMouseEnabledUseCase,
) {

    fun start() {
        scope.launch {
            // The relay dies with the shell, so the mouse goes down and comes back with it.
            combine(mouseSetting, privilegedShellAvailable) { wanted, shellUp -> wanted && shellUp }
                .distinctUntilChanged()
                .collect { setMouseEnabled(it) }
        }
    }
}
