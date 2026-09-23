package com.joegec.joycon2android.buttonmapping

class ApplyPlayerMappingUseCase(
    private val mappingRepository: ControllerMappingRepository,
    private val sidewaysRemoteRepository: SidewaysRemoteRepository,
) {
    suspend operator fun invoke(
        console: Console,
        body: PlayerBody,
        entries: Map<String, String>,
        sidewaysRemote: Boolean,
    ) {
        mappingRepository.replace(console, body, entries)
        sidewaysRemoteRepository.set(console, body, sidewaysRemote)
    }
}
