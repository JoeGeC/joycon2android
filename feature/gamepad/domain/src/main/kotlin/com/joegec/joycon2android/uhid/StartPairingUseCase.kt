package com.joegec.joycon2android.uhid

class StartPairingUseCase(private val repository: WirelessDebugRepository) {
    operator fun invoke() = repository.startPairing()
}
