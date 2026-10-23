package com.joegec.joycon2android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class PrivilegedDevicePolicyTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val setting = MutableStateFlow(false)
    private val shellAvailable = MutableStateFlow(true)
    private val calls = mutableListOf<Boolean>()

    init {
        PrivilegedDevicePolicy(
            scope = scope,
            setting = setting,
            privilegedShellAvailable = shellAvailable,
            setEnabled = { calls += it },
        ).start()
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `the setting turns the device on and off`() {
        setting.value = true
        setting.value = false

        assertEquals(listOf(false, true, false), calls)
    }

    @Test
    fun `the device goes down with the shell and comes back with it`() {
        setting.value = true
        shellAvailable.value = false
        shellAvailable.value = true

        assertEquals(listOf(false, true, false, true), calls)
    }
}
