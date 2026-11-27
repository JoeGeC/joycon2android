package com.joegec.joycon2android.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class HomeWidgetRefreshPolicyTest {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val state = MutableStateFlow(HomeWidgetState())
    private val started = AtomicInteger()
    private val finished = AtomicInteger()

    @After
    fun tearDown() = scope.cancel()

    private fun start(refreshMs: Long = 0) = HomeWidgetRefreshPolicy(scope, state, settleMs = SETTLE_MS) {
        started.incrementAndGet()
        delay(refreshMs)
        finished.incrementAndGet()
    }.start()

    @Test
    fun `a burst of changes refreshes once it settles`() = runBlocking {
        start()
        repeat(5) { state.value = HomeWidgetState(waiting = it + 1) }

        delay(SETTLE_MS * 3)

        assertEquals(1, started.get())
    }

    @Test
    fun `a change during a refresh lets it finish`() = runBlocking {
        start(refreshMs = SETTLE_MS * 2)
        delay(SETTLE_MS * 2)

        state.value = HomeWidgetState(scanning = true)
        delay(SETTLE_MS * 5)

        assertEquals(started.get(), finished.get())
        assertEquals(2, finished.get())
    }

    private companion object {
        const val SETTLE_MS = 50L
    }
}
