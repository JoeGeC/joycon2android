package com.joegec.joycon2android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/** Starts the outputs as players arrive. */
class OutputAutoStartPolicy(
    private val scope: CoroutineScope,
    private val hasPlayers: Flow<Boolean>,
    private val startOutputs: () -> Unit,
) {

    fun start() {
        scope.launch {
            hasPlayers.distinctUntilChanged().filter { it }.collect { startOutputs() }
        }
    }
}
