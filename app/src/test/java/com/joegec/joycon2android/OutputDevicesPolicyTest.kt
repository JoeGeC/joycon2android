package com.joegec.joycon2android

import com.joegec.joycon2android.output.OutputDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class OutputDevicesPolicyTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val devices = MutableStateFlow(emptySet<OutputDevice>())
    private val hasPlayers = MutableStateFlow(true)
    private val pad = mutableListOf<Boolean>()
    private val motion = mutableListOf<Boolean>()

    init {
        OutputDevicesPolicy(scope, devices, hasPlayers, { pad += it }, { motion += it }).start()
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `each device follows the plan`() {
        devices.value = setOf(OutputDevice.PAD, OutputDevice.MOTION)
        devices.value = setOf(OutputDevice.MOTION)

        assertEquals(listOf(false, true, false), pad)
        assertEquals(listOf(false, true), motion)
    }

    @Test
    fun `the pad waits for a player and starts again when one returns`() {
        hasPlayers.value = false
        devices.value = setOf(OutputDevice.PAD)
        hasPlayers.value = true
        hasPlayers.value = false
        hasPlayers.value = true

        assertEquals(listOf(false, true, false, true), pad)
    }
}
