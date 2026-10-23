package com.joegec.joycon2android.output

class SetDeviceOverrideUseCase(private val repository: OutputRepository) {
    suspend operator fun invoke(device: OutputDevice, override: DeviceOverride) = repository.setOverride(device, override)
}
