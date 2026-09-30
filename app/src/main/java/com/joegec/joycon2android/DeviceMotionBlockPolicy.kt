package com.joegec.joycon2android

import com.joegec.joycon2android.settings.SetDeviceMotionBlockedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** docs/dsu-motion.md#eden-reads-the-devices-own-motion */
class DeviceMotionBlockPolicy(
    private val scope: CoroutineScope,
    private val outputActive: Flow<Boolean>,
    private val blockDeviceMotion: Flow<Boolean>,
    private val privilegedShellAvailable: Flow<Boolean>,
    private val setDeviceMotionBlocked: SetDeviceMotionBlockedUseCase,
) {

    fun start() {
        scope.launch {
            // The block can only be set through the shell, so re-apply when it returns.
            combine(outputActive, blockDeviceMotion, privilegedShellAvailable) { active, block, shellUp ->
                (active && block) to shellUp
            }
                .distinctUntilChanged()
                .collect { (blocked, _) -> setDeviceMotionBlocked(blocked) }
        }
    }
}
