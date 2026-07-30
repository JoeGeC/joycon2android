package com.joegec.joycon2android.gamepad

import kotlinx.coroutines.flow.StateFlow

class ObserveShizukuAvailabilityUseCase(private val repository: PrivilegedAccessRepository) {
    operator fun invoke(): StateFlow<Boolean> = repository.shizukuAvailable
}
