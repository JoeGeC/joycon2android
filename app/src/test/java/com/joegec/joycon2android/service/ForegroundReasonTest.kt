package com.joegec.joycon2android.service

import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ForegroundReasonTest {

    private val controller = ConnectedJoycon("L", Side.LEFT, "Joy-Con 2 (L)")

    @Test
    fun `idle needs no foreground`() {
        assertNull(AppUiState().foregroundReason())
    }

    @Test
    fun `scanning alone holds the foreground`() {
        assertEquals(ForegroundReason.SCANNING, AppUiState(scanning = true).foregroundReason())
    }

    @Test
    fun `a connection outranks a scan`() {
        val state = AppUiState(scanning = true, unassignedJoycons = listOf(controller))

        assertEquals(ForegroundReason.CONNECTED, state.foregroundReason())
    }
}
