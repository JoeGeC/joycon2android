package com.joegec.joycon2android.buttonmapping

/** While mapping by press, so the press binds rather than acting. */
class SetDeviceKeysPausedUseCase(private val repository: DeviceKeysPauseRepository) {
    operator fun invoke(paused: Boolean) = repository.setPaused(paused)
}
