package com.joegec.joycon2android.buttonmapping

class DeleteCustomLayoutUseCase(private val savedLayouts: SavedLayoutRepository) {
    suspend operator fun invoke(layoutId: String) = savedLayouts.delete(layoutId)
}
