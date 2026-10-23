package com.joegec.joycon2android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/** Turns the output on as players arrive. Switched off by hand, it stays off until they leave and return. */
class OutputAutoStartPolicy(
    private val scope: CoroutineScope,
    private val hasPlayers: Flow<Boolean>,
    private val setRunning: (Boolean) -> Unit,
) {

    fun start() {
        scope.launch {
            hasPlayers.distinctUntilChanged().filter { it }.collect { setRunning(true) }
        }
    }
}
