package com.joegec.joycon2android

import com.joegec.joycon2android.output.OutputDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Runs the pad and DSU the plan asks for. The pad starts only with a player to give it, and starts
 * again when one comes back, since it stops itself when the last one leaves. It also tries again when
 * Shizuku comes or goes, so its error follows Shizuku.
 */
class OutputDevicesPolicy(
    private val scope: CoroutineScope,
    private val devices: Flow<Set<OutputDevice>>,
    private val hasPlayers: Flow<Boolean>,
    private val privilegedShellAvailable: Flow<Boolean>,
    private val setPad: (Boolean) -> Unit,
    private val setMotion: (Boolean) -> Unit,
) {

    fun start() {
        scope.launch {
            combine(devices, hasPlayers, privilegedShellAvailable) { planned, anyone, shellUp ->
                (OutputDevice.PAD in planned && anyone) to shellUp
            }
                .distinctUntilChanged()
                .collect { (wanted, shellUp) ->
                    // The pad dies with the shell, but still counts itself on, so it would ignore the restart.
                    if (!shellUp) setPad(false)
                    setPad(wanted)
                }
        }
        scope.launch {
            devices.map { OutputDevice.MOTION in it }.distinctUntilChanged().collect(setMotion)
        }
    }
}
