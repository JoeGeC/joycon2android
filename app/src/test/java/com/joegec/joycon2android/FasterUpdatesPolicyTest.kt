package com.joegec.joycon2android

import com.joegec.joycon2android.connection.ConnectionPriorityRepository
import com.joegec.joycon2android.connection.SetHighConnectionPriorityUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class FasterUpdatesPolicyTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val outputActive = MutableStateFlow(false)
    private val fasterUpdates = MutableStateFlow(true)
    private val priorities = mutableListOf<Boolean>()

    init {
        FasterUpdatesPolicy(
            scope = scope,
            outputActive = outputActive,
            fasterUpdates = fasterUpdates,
            setHighConnectionPriority = SetHighConnectionPriorityUseCase(object : ConnectionPriorityRepository {
                override fun setHighPriority(enabled: Boolean) {
                    priorities += enabled
                }
            }),
        ).start()
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `high priority waits for an output`() {
        assertEquals(listOf(false), priorities)
    }

    @Test
    fun `an output turning on raises priority and turning off lowers it`() {
        outputActive.value = true
        outputActive.value = false

        assertEquals(listOf(false, true, false), priorities)
    }

    @Test
    fun `turning the setting off lowers priority while an output runs`() {
        outputActive.value = true
        fasterUpdates.value = false

        assertEquals(listOf(false, true, false), priorities)
    }
}
