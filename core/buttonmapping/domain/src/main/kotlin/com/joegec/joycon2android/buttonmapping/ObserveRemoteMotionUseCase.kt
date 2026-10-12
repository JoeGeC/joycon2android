package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveRemoteMotionUseCase(
    private val observeSidewaysRemote: ObserveSidewaysRemoteUseCase,
    private val observeThrust: ObserveThrustUseCase,
    private val observeSensitivity: ObserveSensitivityUseCase,
) {
    operator fun invoke(console: Console, body: PlayerBody): Flow<RemoteMotion> = combine(
        observeSidewaysRemote(console, body),
        observeThrust(console, body),
        observeSensitivity(console, body),
        ::RemoteMotion,
    )
}
