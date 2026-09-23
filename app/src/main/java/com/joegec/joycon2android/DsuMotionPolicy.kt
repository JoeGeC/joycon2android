package com.joegec.joycon2android

import com.joegec.joycon2android.connection.SetHighConnectionPriorityUseCase
import com.joegec.joycon2android.dsu.motion.DsuMotionSettings
import com.joegec.joycon2android.dsu.motion.SetDeviceMotionBlockedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** Motion settings apply only while DSU runs: docs/dsu-motion.md#eden-reads-the-devices-own-motion */
class DsuMotionPolicy(
    private val scope: CoroutineScope,
    private val dsuEnabled: Flow<Boolean>,
    private val settings: Flow<DsuMotionSettings>,
    private val privilegedShellAvailable: Flow<Boolean>,
    private val setHighConnectionPriority: SetHighConnectionPriorityUseCase,
    private val setDeviceMotionBlocked: SetDeviceMotionBlockedUseCase,
) {

    fun start() {
        scope.launch {
            combine(dsuEnabled, settings) { dsuOn, current -> dsuOn && current.fastMotion }
                .distinctUntilChanged()
                .collect { setHighConnectionPriority(it) }
        }
        scope.launch {
            // The block can only be set through the shell, so re-apply when it returns.
            combine(dsuEnabled, settings, privilegedShellAvailable) { dsuOn, current, shellUp ->
                (dsuOn && current.blockDeviceMotion) to shellUp
            }
                .distinctUntilChanged()
                .collect { (blocked, _) -> setDeviceMotionBlocked(blocked) }
        }
    }
}
