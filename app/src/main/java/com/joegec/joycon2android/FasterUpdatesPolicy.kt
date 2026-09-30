package com.joegec.joycon2android

import com.joegec.joycon2android.connection.SetHighConnectionPriorityUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** docs/protocol.md#android-ble-gotchas */
class FasterUpdatesPolicy(
    private val scope: CoroutineScope,
    private val outputActive: Flow<Boolean>,
    private val fasterUpdates: Flow<Boolean>,
    private val setHighConnectionPriority: SetHighConnectionPriorityUseCase,
) {

    fun start() {
        scope.launch {
            combine(outputActive, fasterUpdates) { active, faster -> active && faster }
                .distinctUntilChanged()
                .collect { setHighConnectionPriority(it) }
        }
    }
}
