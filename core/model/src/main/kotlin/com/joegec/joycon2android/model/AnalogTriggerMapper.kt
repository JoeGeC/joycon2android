package com.joegec.joycon2android.model

/** Why ZL and ZR move off L2/R2: docs/virtual-gamepad.md#analog-triggers */
object AnalogTriggerMapper {

    fun remapButtons(pressed: Set<String>): Set<String> =
        pressed.mapTo(mutableSetOf()) { REMAP[it] ?: it }

    /** Null for a stick click, whose bits carry ZL or ZR instead. */
    fun emittedId(id: String): String? = REMAP[id] ?: id.takeUnless { it in REMAP.values }

    // The stick clicks are free: a controller with analog triggers has none.
    private val REMAP = mapOf(
        JoyconButton.ZL.id to JoyconButton.LS.id,
        JoyconButton.ZR.id to JoyconButton.RS.id,
    )
}
