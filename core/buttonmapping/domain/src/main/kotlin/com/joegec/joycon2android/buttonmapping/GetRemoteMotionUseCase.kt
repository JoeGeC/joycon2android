package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

/** One-shot read, for the emulator-config generators at "Set up" time. */
class GetRemoteMotionUseCase(private val observeRemoteMotion: ObserveRemoteMotionUseCase) {
    suspend operator fun invoke(console: Console, body: PlayerBody): RemoteMotion =
        observeRemoteMotion(console, body).first()
}
