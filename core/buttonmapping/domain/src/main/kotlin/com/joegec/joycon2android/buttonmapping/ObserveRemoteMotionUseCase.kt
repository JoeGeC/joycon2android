package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveRemoteMotionUseCase(
    private val observeWiiController: ObserveWiiControllerUseCase,
    private val observeThrust: ObserveThrustUseCase,
    private val observeSensitivity: ObserveSensitivityUseCase,
) {
    operator fun invoke(console: Console, body: PlayerBody): Flow<RemoteMotion> = combine(
        observeWiiController(console, body),
        observeThrust(console, body),
        observeSensitivity(console, body),
        ::RemoteMotion,
    )
}
