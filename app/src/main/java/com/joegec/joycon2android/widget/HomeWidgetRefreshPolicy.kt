package com.joegec.joycon2android.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/** A widget outside an open Glance session redraws only when asked: docs/architecture.md#home-screen-widget */
class HomeWidgetRefreshPolicy(
    private val scope: CoroutineScope,
    private val state: Flow<HomeWidgetState>,
    private val settleMs: Long = SETTLE_MS,
    private val refresh: suspend () -> Unit,
) {
    @OptIn(FlowPreview::class)
    fun start() {
        scope.launch { state.debounce(settleMs).collect { refresh() } }
    }

    private companion object {
        const val SETTLE_MS = 500L
    }
}
