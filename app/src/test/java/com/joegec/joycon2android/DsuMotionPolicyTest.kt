package com.joegec.joycon2android

import com.joegec.joycon2android.connection.ConnectionPriorityRepository
import com.joegec.joycon2android.connection.SetHighConnectionPriorityUseCase
import com.joegec.joycon2android.dsu.motion.DeviceMotionBlocker
import com.joegec.joycon2android.dsu.motion.DsuMotionSettings
import com.joegec.joycon2android.dsu.motion.SetDeviceMotionBlockedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class DsuMotionPolicyTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val dsuEnabled = MutableStateFlow(false)
    private val settings = MutableStateFlow(DsuMotionSettings())
    private val shellAvailable = MutableStateFlow(true)
    private val priorities = mutableListOf<Boolean>()
    private val blocks = mutableListOf<Boolean>()

    init {
        DsuMotionPolicy(
            scope = scope,
            dsuEnabled = dsuEnabled,
            settings = settings,
            privilegedShellAvailable = shellAvailable,
            setHighConnectionPriority = SetHighConnectionPriorityUseCase(object : ConnectionPriorityRepository {
                override fun setHighPriority(enabled: Boolean) {
                    priorities += enabled
                }
            }),
            setDeviceMotionBlocked = SetDeviceMotionBlockedUseCase(object : DeviceMotionBlocker {
                override suspend fun setBlocked(blocked: Boolean) {
                    blocks += blocked
                }
            }),
        ).start()
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `launch lifts any block a killed process left behind`() {
        assertEquals(listOf(false), blocks)
    }

    @Test
    fun `defaults block device motion but keep normal priority while DSU runs`() {
        dsuEnabled.value = true

        assertEquals(listOf(false, true), blocks)
        assertEquals(listOf(false), priorities)
    }

    @Test
    fun `turning DSU off lifts the block and high priority`() {
        settings.value = DsuMotionSettings(fastMotion = true)
        dsuEnabled.value = true
        dsuEnabled.value = false

        assertEquals(listOf(false, true, false), blocks)
        assertEquals(listOf(false, true, false), priorities)
    }

    @Test
    fun `turning the block setting off lifts it while DSU keeps running`() {
        dsuEnabled.value = true
        settings.value = DsuMotionSettings(blockDeviceMotion = false)

        assertEquals(listOf(false, true, false), blocks)
    }

    @Test
    fun `the block is reapplied when the privileged shell returns`() {
        dsuEnabled.value = true
        shellAvailable.value = false
        shellAvailable.value = true

        assertEquals(listOf(false, true, true, true), blocks)
    }
}
