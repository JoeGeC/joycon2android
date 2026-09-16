package com.joegec.joycon2android.dsu.motion

class SetDeviceMotionBlockedUseCase(private val blocker: DeviceMotionBlocker) {
    suspend operator fun invoke(blocked: Boolean) = blocker.setBlocked(blocked)
}
