package com.joegec.joycon2android.buttonmapping

class ApplyPlayerMappingUseCase(
    private val mappingRepository: ControllerMappingRepository,
    private val wiiControllerRepository: WiiControllerRepository,
    private val thrustRepository: ThrustRepository,
    private val sensitivityRepository: SensitivityRepository,
) {
    suspend operator fun invoke(
        console: Console,
        body: PlayerBody,
        entries: Map<String, String>,
        motion: RemoteMotion,
    ) {
        mappingRepository.replace(console, body, entries)
        wiiControllerRepository.set(console, body, motion.controller)
        thrustRepository.set(console, body, motion.thrust)
        sensitivityRepository.set(console, body, motion.sensitivityPercent)
    }
}
