package com.joegec.joycon2android.buttonmapping

/** Only the name goes: every player keeps the bindings, which read as Custom until it is saved back. */
class DeleteCustomLayoutUseCase(private val savedLayouts: SavedLayoutRepository) {
    suspend operator fun invoke(layoutId: String) = savedLayouts.delete(layoutId)
}
