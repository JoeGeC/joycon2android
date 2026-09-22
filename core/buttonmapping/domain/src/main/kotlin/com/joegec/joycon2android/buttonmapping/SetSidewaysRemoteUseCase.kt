package com.joegec.joycon2android.buttonmapping

/** Records the user's own answer, which from then on outranks the layout's. */
class SetSidewaysRemoteUseCase(private val repository: SidewaysRemoteRepository) {
    suspend operator fun invoke(console: Console, body: PlayerBody, enabled: Boolean) =
        repository.set(console, body, enabled)
}
