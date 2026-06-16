package com.joegec.joycon2android.gamepad.wirelessdebug

class StartPairingUseCase(private val repository: WirelessDebugRepository) {
    operator fun invoke() = repository.startPairing()
}
