package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceKeysPauseTest {

    private val pause = DeviceKeysPause()

    private fun player(pressed: Set<String> = emptySet(), stickY: Int = 2048) = PlayerState(
        PlayerNumber.P1,
        left = ConnectedJoycon("l", Side.LEFT, "L", input = JoyconInput(pressed = pressed, stickY = stickY)),
    )

    @Test
    fun `unpausing waits until the button that ended it lifts`() {
        pause.setPaused(true)
        pause.setPaused(false)

        pause.onFrame(listOf(player(setOf("B"))))
        assertTrue(pause.paused.value)

        pause.onFrame(listOf(player()))
        assertFalse(pause.paused.value)
    }

    @Test
    fun `a stick still pushed holds the pause too`() {
        pause.setPaused(true)
        pause.setPaused(false)

        pause.onFrame(listOf(player(stickY = 4000)))

        assertTrue(pause.paused.value)
    }

    @Test
    fun `a controller at rest is no reason to unpause before being asked`() {
        pause.setPaused(true)

        pause.onFrame(listOf(player()))

        assertTrue(pause.paused.value)
    }

    @Test
    fun `pausing again before the release keeps it paused`() {
        pause.setPaused(true)
        pause.setPaused(false)
        pause.setPaused(true)

        pause.onFrame(listOf(player()))

        assertTrue(pause.paused.value)
    }
}
