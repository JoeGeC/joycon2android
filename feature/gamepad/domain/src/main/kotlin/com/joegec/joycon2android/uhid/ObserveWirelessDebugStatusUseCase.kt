package com.joegec.joycon2android.uhid

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveWirelessDebugStatusUseCase(private val repository: WirelessDebugRepository) {
    operator fun invoke(): Flow<WirelessDebugStatus> = combine(
        repository.adbState,
        repository.adbError,
        repository.pairingServiceAvailable,
    ) { state, error, pairing ->
        WirelessDebugStatus(state, error, pairing)
    }
}
