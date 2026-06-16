package com.joegec.joycon2android.gamepad

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveGamepadStatusUseCase(private val repository: GamepadRepository) {
    operator fun invoke(): Flow<GamepadStatus> =
        combine(repository.enabled, repository.error) { enabled, error ->
            GamepadStatus(enabled, error)
        }
}
