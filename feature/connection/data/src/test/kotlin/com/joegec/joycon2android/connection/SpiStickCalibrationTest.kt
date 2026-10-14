package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpiStickCalibrationTest {

    // A GameCube controller's main stick as captured: rest 2073,2060, travel 835..3297 x 811..3258.
    private val mainStick = StickCalibration(
        centreX = 2073, centreY = 2060, aboveX = 1224, aboveY = 1198, belowX = 1238, belowY = 1249,
    )

    @Test
    fun `the nine bytes decode as centre, span above and span below`() {
        assertEquals(mainStick, SpiStickCalibration.parse(reply(SpiStickCalibration.MAIN_ADDRESS, mainStick), SpiStickCalibration.MAIN_ADDRESS))
    }

    @Test
    fun `unset flash reads as no calibration`() {
        val unset = reply(SpiStickCalibration.MAIN_ADDRESS, ByteArray(9) { 0xFF.toByte() })
        assertNull(SpiStickCalibration.parse(unset, SpiStickCalibration.MAIN_ADDRESS))
    }

    @Test
    fun `a reply for the other stick is not this read`() {
        assertNull(SpiStickCalibration.parse(reply(SpiStickCalibration.RIGHT_ADDRESS, mainStick), SpiStickCalibration.MAIN_ADDRESS))
    }

    @Test
    fun `only a gamecube controller reads its flash, both sticks and the trigger zeros`() {
        assertEquals(0, FactoryCalibrationReads.commands(ControllerModel.JOYCON_RIGHT).size)
        assertEquals(0, FactoryCalibrationReads.commands(ControllerModel.PRO_CONTROLLER).size)
        assertEquals(3, FactoryCalibrationReads.commands(ControllerModel.GAMECUBE).size)
    }

    @Test
    fun `each reply fills its own part of the factory calibration`() {
        val withMain = FactoryCalibrationReads.update(FactoryCalibration(), reply(SpiStickCalibration.MAIN_ADDRESS, mainStick))!!
        val withRight = FactoryCalibrationReads.update(withMain, reply(SpiStickCalibration.RIGHT_ADDRESS, mainStick))!!
        assertEquals(mainStick, withRight.mainStick)
        assertEquals(mainStick, withRight.rightStick)
        assertNull(withRight.triggerZeroLeft)
    }

    @Test
    fun `a reply carrying nothing it reads adds nothing`() {
        assertNull(FactoryCalibrationReads.update(FactoryCalibration(), reply(0x013000, ByteArray(9))))
    }

    private fun reply(address: Int, calibration: StickCalibration): ByteArray = reply(
        address,
        pack(calibration.centreX, calibration.centreY) + pack(calibration.aboveX, calibration.aboveY) +
            pack(calibration.belowX, calibration.belowY),
    )

    private fun reply(address: Int, data: ByteArray): ByteArray =
        byteArrayOf(0x02, 0x01, 0x00, 0x04, 0x10, 0x78, 0x00, 0x00, data.size.toByte(), 0x00, 0x00, 0x00) +
            byteArrayOf(address.toByte(), (address shr 8).toByte(), (address shr 16).toByte(), 0x00) + data

    private fun pack(x: Int, y: Int): ByteArray =
        byteArrayOf(x.toByte(), ((x shr 8) and 0x0F or ((y and 0x0F) shl 4)).toByte(), (y shr 4).toByte())
}
