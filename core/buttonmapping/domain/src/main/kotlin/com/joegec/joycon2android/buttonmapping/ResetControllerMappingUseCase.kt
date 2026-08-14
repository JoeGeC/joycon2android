package com.joegec.joycon2android.buttonmapping

/** Discards every override for a console/body, reverting it to the shipped defaults. */
class ResetControllerMappingUseCase(private val repository: ControllerMappingRepository) {
    suspend operator fun invoke(console: Console, side: JoyconSide) = repository.clear(console, side)
}
