package com.joegec.joycon2android.mouse

import com.joegec.joycon2android.gamepad.mouse.MappedMouse
import com.joegec.joycon2android.gamepad.mouse.MouseFrame
import com.joegec.joycon2android.model.MouseAction

/** One report a packet from the desk mouse and the mapping together, since a report's buttons replace the last's. */
class MouseMix(private val mapped: MappedMouse, private val push: (MouseFrame) -> Unit) {

    private var desk = MouseFrame()

    fun desk(frame: MouseFrame) {
        desk = frame
    }

    fun send(held: Set<MouseAction>) {
        push(desk + mapped.frame(held))
        desk = MouseFrame()
    }
}
