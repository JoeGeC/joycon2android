package com.joegec.joycon2android.connection

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ControllerCommandsTest {

    // The frames both protocols sent before one builder produced them.
    @Test
    fun `builds the feature commands`() {
        assertArrayEquals(
            bytes(0x0C, 0x91, 0x01, 0x02, 0x00, 0x04, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00),
            ControllerCommands.selectFeatures(0xFF.toByte()),
        )
        assertArrayEquals(
            bytes(0x0C, 0x91, 0x01, 0x04, 0x00, 0x04, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00),
            ControllerCommands.enableFeatures(0xFF.toByte()),
        )
        assertArrayEquals(
            bytes(0x0C, 0x91, 0x01, 0x02, 0x00, 0x04, 0x00, 0x00, 0x37, 0x00, 0x00, 0x00),
            ControllerCommands.selectFeatures(0x37),
        )
    }

    @Test
    fun `builds the player LED command`() {
        assertArrayEquals(
            bytes(0x09, 0x91, 0x01, 0x07, 0x00, 0x08, 0x00, 0x00, 0x0F, 0, 0, 0, 0, 0, 0, 0),
            ControllerCommands.playerLed(ControllerCommands.LED_ALL_ON),
        )
        assertArrayEquals(
            bytes(0x09, 0x91, 0x01, 0x07, 0x00, 0x08, 0x00, 0x00, 0x01, 0, 0, 0, 0, 0, 0, 0),
            ControllerCommands.playerLed(0x01),
        )
    }

    // The common path has always read the colour over transport 0x00, the console one over 0x01.
    @Test
    fun `builds the SPI read on either transport`() {
        assertArrayEquals(
            bytes(0x02, 0x91, 0x00, 0x04, 0x00, 0x08, 0x00, 0x00, 0x40, 0x7E, 0x00, 0x00, 0x00, 0x30, 0x01, 0x00),
            ControllerCommands.spiRead(0x40, ControllerCommands.DEVICE_INFO_ADDRESS, ControllerCommands.USB),
        )
        assertArrayEquals(
            bytes(0x02, 0x91, 0x01, 0x04, 0x00, 0x08, 0x00, 0x00, 0x40, 0x7E, 0x00, 0x00, 0x00, 0x30, 0x01, 0x00),
            ControllerCommands.spiRead(0x40, ControllerCommands.DEVICE_INFO_ADDRESS),
        )
        assertArrayEquals(
            bytes(0x02, 0x91, 0x01, 0x04, 0x00, 0x08, 0x00, 0x00, 0x18, 0x7E, 0x00, 0x00, 0x00, 0x31, 0x01, 0x00),
            ControllerCommands.spiRead(0x18, 0x013100),
        )
    }

    @Test
    fun `a command with no data still carries a full header`() {
        assertArrayEquals(
            bytes(0x07, 0x91, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00),
            ControllerCommands.command(0x07, 0x01),
        )
    }

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }
}
