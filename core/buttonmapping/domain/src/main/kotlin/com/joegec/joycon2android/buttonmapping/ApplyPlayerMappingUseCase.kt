package com.joegec.joycon2android.buttonmapping

/**
 * Writes a body's whole mapping at once. Everything a player is given is written out in full, so
 * nothing they play with depends on a layout that can later be deleted.
 */
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
