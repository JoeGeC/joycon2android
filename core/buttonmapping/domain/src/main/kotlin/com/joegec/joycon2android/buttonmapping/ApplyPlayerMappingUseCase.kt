package com.joegec.joycon2android.buttonmapping

class ApplyPlayerMappingUseCase(
    private val mappingRepository: ControllerMappingRepository,
    private val sidewaysRemoteRepository: SidewaysRemoteRepository,
    private val thrustRepository: ThrustRepository,
) {
    suspend operator fun invoke(
        console: Console,
        body: PlayerBody,
        entries: Map<String, String>,
        motion: RemoteMotion,
    ) {
        mappingRepository.replace(console, body, entries)
        sidewaysRemoteRepository.set(console, body, motion.sideways)
        thrustRepository.set(console, body, motion.thrust)
    }
}
