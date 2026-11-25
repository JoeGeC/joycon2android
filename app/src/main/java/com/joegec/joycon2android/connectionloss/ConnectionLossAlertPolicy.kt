package com.joegec.joycon2android.connectionloss

import com.joegec.joycon2android.model.AppUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ConnectionLossAlertPolicy(
    private val scope: CoroutineScope,
    private val session: Flow<AppUiState>,
    private val notify: Flow<Boolean>,
    private val show: (LostController) -> Unit,
    private val clear: (address: String) -> Unit,
) {
    private val watch = ConnectionLossWatch()

    fun start() {
        scope.launch {
            session
                .map { it.lostControllers() to it.connectedAddresses() }
                .distinctUntilChanged()
                .map { (lost, connected) -> watch.update(lost, connected) }
                .collect { change -> alert(change) }
        }
    }

    private suspend fun alert(change: ConnectionLossChange) {
        change.returned.forEach(clear)
        if (change.lost.isNotEmpty() && notify.first()) change.lost.forEach(show)
    }
}
