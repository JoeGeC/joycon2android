package com.joegec.joycon2android.gamepad

class PushGamepadStateUseCase(private val repository: GamepadRepository) {
    operator fun invoke(frames: List<GamepadFrame>) = repository.push(frames)
}
