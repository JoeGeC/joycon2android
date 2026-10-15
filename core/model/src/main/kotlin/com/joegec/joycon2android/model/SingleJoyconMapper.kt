package com.joegec.joycon2android.model

/** docs/virtual-gamepad.md#single-joy-con */
object SingleJoyconMapper {

    fun remapRails(pressed: Set<String>): Set<String> =
        pressed.mapTo(mutableSetOf()) { RAILS[it] ?: it }

    // The shoulders the body lacks, as SidewaysMapper uses, so the HID report needs no new bits.
    private val RAILS = mapOf(
        JoyconButton.SlLeft.id to JoyconButton.R.id,
        JoyconButton.SrLeft.id to JoyconButton.ZR.id,
        JoyconButton.SlRight.id to JoyconButton.L.id,
        JoyconButton.SrRight.id to JoyconButton.ZL.id,
    )
}
