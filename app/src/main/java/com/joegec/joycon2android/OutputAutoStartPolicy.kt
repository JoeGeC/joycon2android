package com.joegec.joycon2android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/** Starts the tabs last left on as players arrive. A tab switched off by hand is no longer among them. */
class OutputAutoStartPolicy(
    private val scope: CoroutineScope,
    private val hasPlayers: Flow<Boolean>,
    private val startRemembered: suspend () -> Unit,
) {

    fun start() {
        scope.launch {
            hasPlayers.distinctUntilChanged().filter { it }.collect { startRemembered() }
        }
    }
}
