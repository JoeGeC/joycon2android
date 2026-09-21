package com.joegec.joycon2android.buttonmapping

/**
 * Switches a console to another layout. Overrides only make sense against the layout they were made
 * on, so the console's customizations — its bindings and its sideways-remote switch — go with the
 * old one.
 */
class ApplyMappingPresetUseCase(
    private val presetRepository: MappingPresetRepository,
    private val mappingRepository: ControllerMappingRepository,
    private val sidewaysRemoteRepository: SidewaysRemoteRepository,
) {
    suspend operator fun invoke(console: Console, presetId: String) {
        presetRepository.set(console, presetId)
        sidewaysRemoteRepository.clear(console)
        JoyconSide.entries.forEach { mappingRepository.clear(console, it) }
    }
}
