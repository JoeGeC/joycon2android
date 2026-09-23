package com.joegec.joycon2android.buttonmapping

/** Overrides the layout's answer until a layout is next applied. */
class SetSidewaysRemoteUseCase(private val repository: SidewaysRemoteRepository) {
    suspend operator fun invoke(console: Console, body: PlayerBody, enabled: Boolean) =
        repository.set(console, body, enabled)
}
