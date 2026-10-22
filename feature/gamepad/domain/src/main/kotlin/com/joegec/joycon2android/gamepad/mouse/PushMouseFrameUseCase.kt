package com.joegec.joycon2android.gamepad.mouse

class PushMouseFrameUseCase(private val repository: MouseRepository) {
    operator fun invoke(frame: MouseFrame) = repository.push(frame)
}
