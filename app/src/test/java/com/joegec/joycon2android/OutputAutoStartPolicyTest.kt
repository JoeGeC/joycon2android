package com.joegec.joycon2android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class OutputAutoStartPolicyTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val hasPlayers = MutableStateFlow(false)
    private var starts = 0

    init {
        OutputAutoStartPolicy(scope, hasPlayers) { starts++ }.start()
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `the first player starts the remembered tabs, and nothing before`() {
        assertEquals(0, starts)

        hasPlayers.value = true

        assertEquals(1, starts)
    }

    @Test
    fun `players coming back start them again, not each change in between`() {
        hasPlayers.value = true
        hasPlayers.value = false
        hasPlayers.value = true

        assertEquals(2, starts)
    }
}
