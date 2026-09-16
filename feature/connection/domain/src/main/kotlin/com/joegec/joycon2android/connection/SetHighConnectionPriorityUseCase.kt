package com.joegec.joycon2android.connection

class SetHighConnectionPriorityUseCase(private val repository: ConnectionPriorityRepository) {
    operator fun invoke(enabled: Boolean) = repository.setHighPriority(enabled)
}
