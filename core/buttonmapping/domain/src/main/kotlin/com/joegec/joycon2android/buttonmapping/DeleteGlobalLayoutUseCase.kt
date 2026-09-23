package com.joegec.joycon2android.buttonmapping

class DeleteGlobalLayoutUseCase(private val globalLayouts: GlobalLayoutRepository) {
    suspend operator fun invoke(layoutId: String) = globalLayouts.delete(layoutId)
}
