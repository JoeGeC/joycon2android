package com.joegec.joycon2android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveInputTest {

    private fun session(
        packetId: Int,
        stickX: Int,
        battery: BatteryCharge? = BatteryCharge(80),
        mouse: MouseSensor? = null,
    ) = AppUiState(
        players = listOf(
            PlayerState(
                PlayerNumber.P1,
                right = ConnectedJoycon(
                    address = "R",
                    side = Side.RIGHT,
                    deviceName = "Joy-Con",
                    input = JoyconInput(packetId = packetId, stickX = stickX, battery = battery, mouse = mouse),
                ),
            ),
        ),
    )

    @Test
    fun `without live input two packets read as the same session`() {
        assertEquals(session(1, 100).withoutLiveInput(), session(2, 3000).withoutLiveInput())
    }

    @Test
    fun `without live input a battery change still shows`() {
        assertNotEquals(session(1, 100).withoutLiveInput(), session(1, 100, BatteryCharge(20)).withoutLiveInput())
    }

    @Test
    fun `without live input a Joy-Con still reads as able to move the pointer`() {
        val stripped = session(1, 100, mouse = MouseSensor(x = 5, y = 0, distance = 150)).withoutLiveInput()

        assertTrue(stripped.players.single().canMovePointer)
    }

    @Test
    fun `live input puts each controller's packet back by address`() {
        val live = session(7, 3000)

        val restored = live.withoutLiveInput().players.single().withLiveInput(live.liveInputs)

        assertEquals(live.players.single(), restored)
    }
}
