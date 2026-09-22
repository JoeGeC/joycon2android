package com.joegec.joycon2android.buttonmapping

/** Records the user's choice of physical source for one target button or stick. */
class SetControllerMappingUseCase(private val repository: ControllerMappingRepository) {
    suspend operator fun invoke(console: Console, body: PlayerBody, targetKey: String, sourceId: String) =
        repository.set(console, body, targetKey, sourceId)
}
