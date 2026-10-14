package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel

/** docs/protocol.md#spi-reads */
object FactoryCalibrationReads {

    // Only the GameCube controller's flash has been checked against its sticks and triggers.
    fun commands(model: ControllerModel): List<ByteArray> =
        if (model != ControllerModel.GAMECUBE) {
            emptyList()
        } else {
            listOf(
                spiRead(SpiStickCalibration.MAIN_ADDRESS, SpiStickCalibration.LENGTH),
                spiRead(SpiStickCalibration.RIGHT_ADDRESS, SpiStickCalibration.LENGTH),
                spiRead(SpiTriggerZeros.ADDRESS, SpiTriggerZeros.LENGTH),
            )
        }

    // Transport 0x00, as the colour read sends it.
    private fun spiRead(address: Int, length: Int) = ControllerCommands.spiRead(length, address, ControllerCommands.USB)

    /** Null when [reply] adds nothing: another read, or flash left unset. */
    fun update(factory: FactoryCalibration, reply: ByteArray): FactoryCalibration? {
        SpiStickCalibration.parse(reply, SpiStickCalibration.MAIN_ADDRESS)?.let { return factory.copy(mainStick = it) }
        SpiStickCalibration.parse(reply, SpiStickCalibration.RIGHT_ADDRESS)?.let { return factory.copy(rightStick = it) }
        SpiTriggerZeros.parse(reply)?.let { (left, right) ->
            return factory.copy(triggerZeroLeft = left, triggerZeroRight = right)
        }
        return null
    }
}
