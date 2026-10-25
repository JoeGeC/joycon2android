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
    private val running = mutableListOf<Boolean>()

    init {
        OutputAutoStartPolicy(scope, hasPlayers) { running += it }.start()
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `the first player turns the output on, and nothing before`() {
        assertEquals(emptyList<Boolean>(), running)

        hasPlayers.value = true

        assertEquals(listOf(true), running)
    }

    @Test
    fun `players coming back turn it on again, not each change in between`() {
        hasPlayers.value = true
        hasPlayers.value = false
        hasPlayers.value = true

        assertEquals(listOf(true, true), running)
    }
}
