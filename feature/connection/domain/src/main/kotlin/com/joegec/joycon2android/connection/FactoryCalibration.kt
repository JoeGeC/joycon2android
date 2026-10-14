package com.joegec.joycon2android.connection

/** What the controller's SPI flash holds; null where it is unread or unset. docs/protocol.md#spi-reads */
data class FactoryCalibration(
    val mainStick: StickCalibration? = null,
    val rightStick: StickCalibration? = null,
    val triggerZeroLeft: Int? = null,
    val triggerZeroRight: Int? = null,
)
