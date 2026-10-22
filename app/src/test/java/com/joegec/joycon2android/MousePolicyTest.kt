package com.joegec.joycon2android

import com.joegec.joycon2android.gamepad.mouse.MouseFrame
import com.joegec.joycon2android.gamepad.mouse.MouseRepository
import com.joegec.joycon2android.gamepad.mouse.SetMouseEnabledUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class MousePolicyTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val setting = MutableStateFlow(false)
    private val shellAvailable = MutableStateFlow(true)
    private val calls = mutableListOf<Boolean>()

    init {
        MousePolicy(
            scope = scope,
            mouseSetting = setting,
            privilegedShellAvailable = shellAvailable,
            setMouseEnabled = SetMouseEnabledUseCase(object : MouseRepository {
                override val enabled: StateFlow<Boolean> = MutableStateFlow(false)
                override fun enable() { calls += true }
                override fun disable() { calls += false }
                override fun push(frame: MouseFrame) = Unit
            }),
        ).start()
    }

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `the setting turns the mouse on and off`() {
        setting.value = true
        setting.value = false

        assertEquals(listOf(false, true, false), calls)
    }

    @Test
    fun `the mouse goes down with the shell and comes back with it`() {
        setting.value = true
        shellAvailable.value = false
        shellAvailable.value = true

        assertEquals(listOf(false, true, false, true), calls)
    }
}
