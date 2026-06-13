package com.joegec.joycon2android.dsu

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveDsuStatusUseCase(private val repository: DsuRepository) {
    operator fun invoke(): Flow<DsuStatus> = combine(
        repository.enabled,
        repository.error,
        repository.clientCount,
        repository.lanEnabled,
        repository.address,
    ) { enabled, error, clientCount, lanEnabled, address ->
        DsuStatus(enabled, error, clientCount, lanEnabled, address)
    }
}
