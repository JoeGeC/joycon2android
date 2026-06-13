package com.joegec.joycon2android.uhid

class StartWirelessDiscoveryUseCase(private val repository: WirelessDebugRepository) {
    operator fun invoke() = repository.startDiscovery()
}
