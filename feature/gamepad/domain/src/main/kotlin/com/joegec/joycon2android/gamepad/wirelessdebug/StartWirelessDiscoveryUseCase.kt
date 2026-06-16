package com.joegec.joycon2android.gamepad.wirelessdebug

class StartWirelessDiscoveryUseCase(private val repository: WirelessDebugRepository) {
    operator fun invoke() = repository.startDiscovery()
}
