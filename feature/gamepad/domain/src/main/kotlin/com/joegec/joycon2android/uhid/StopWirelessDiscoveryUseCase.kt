package com.joegec.joycon2android.uhid

class StopWirelessDiscoveryUseCase(private val repository: WirelessDebugRepository) {
    operator fun invoke() = repository.stopDiscovery()
}
