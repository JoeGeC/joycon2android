package com.joegec.joycon2android

import com.joegec.joycon2android.settings.DeviceMotionBlocker
import com.joegec.joycon2android.settings.SetDeviceMotionBlockedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceMotionBlockPolicyTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val outputActive = MutableStateFlow(false)
    private val blockDeviceMotion = MutableStateFlow(true)
    private val shellAvailable = MutableStateFlow(true)
    private val blocks = mutableListOf<Boolean>()

    init {
        DeviceMotionBlockPolicy(
            scope = scope,
            outputActive = outputActive,
            blockDeviceMotion = blockDeviceMotion,
            privilegedShellAvailable = shellAvailable,
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
    fun `an output turning on blocks and turning off lifts`() {
        outputActive.value = true
        outputActive.value = false

        assertEquals(listOf(false, true, false), blocks)
    }

    @Test
    fun `turning the setting off lifts the block while an output runs`() {
        outputActive.value = true
        blockDeviceMotion.value = false

        assertEquals(listOf(false, true, false), blocks)
    }

    @Test
    fun `the block is reapplied when the privileged shell returns`() {
        outputActive.value = true
        shellAvailable.value = false
        shellAvailable.value = true

        assertEquals(listOf(false, true, true, true), blocks)
    }
}
