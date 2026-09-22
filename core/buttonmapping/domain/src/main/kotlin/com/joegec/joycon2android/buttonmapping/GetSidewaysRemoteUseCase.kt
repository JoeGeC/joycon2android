package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

/** One-shot read, for the emulator-config generators at "Set up" time. */
class GetSidewaysRemoteUseCase(private val observeSidewaysRemote: ObserveSidewaysRemoteUseCase) {
    suspend operator fun invoke(console: Console, body: PlayerBody): Boolean =
        observeSidewaysRemote(console, body).first()
}
