package com.joegec.joycon2android.buttonmapping

/** Deleting a saved set leaves every player exactly where they are; only the name goes. */
class DeleteGlobalLayoutUseCase(private val globalLayouts: GlobalLayoutRepository) {
    suspend operator fun invoke(layoutId: String) = globalLayouts.delete(layoutId)
}
