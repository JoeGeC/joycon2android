package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Rumble

internal class HdRumbleEncoder(private val motors: Int) : RumbleEncoder {

    private var counter = 0

    override fun packet(rumble: Rumble) = HdRumble.packet(counter++, rumble, motors)

    override fun packet(amplitude: Int) = HdRumble.packet(counter++, amplitude, amplitude, motors)
}
