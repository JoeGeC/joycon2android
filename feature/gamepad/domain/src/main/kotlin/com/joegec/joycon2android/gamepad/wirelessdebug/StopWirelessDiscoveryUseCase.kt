package com.joegec.joycon2android.gamepad.wirelessdebug

class StopWirelessDiscoveryUseCase(private val repository: WirelessDebugRepository) {
    operator fun invoke() = repository.stopDiscovery()
}
