package com.joegec.joycon2android

import com.joegec.joycon2android.connection.SetHighConnectionPriorityUseCase
import com.joegec.joycon2android.dsu.motion.DsuMotionSettings
import com.joegec.joycon2android.dsu.motion.SetDeviceMotionBlockedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Applies the DSU motion settings only while DSU runs, so neither the battery cost nor the
 * sensor block lingers once it stops. DSU starts off, so launch also clears a block left behind
 * by a process that was killed before it could lift it.
 */
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
            // Re-applied when the shell comes back, since the block can only be set through it.
            combine(dsuEnabled, settings, privilegedShellAvailable) { dsuOn, current, shellUp ->
                (dsuOn && current.blockDeviceMotion) to shellUp
            }
                .distinctUntilChanged()
                .collect { (blocked, _) -> setDeviceMotionBlocked(blocked) }
        }
    }
}
