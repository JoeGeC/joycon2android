package com.joegec.joycon2android.model

/** Where each shoulder goes, and why: docs/virtual-gamepad.md#analog-triggers */
object AnalogTriggerMapper {

    fun remapButtons(pressed: Set<String>): Set<String> =
        pressed.mapTo(mutableSetOf()) { REMAP[it] ?: it }

    /** Null for a stick click, whose bits carry the trigger clicks instead. */
    fun emittedId(id: String): String? = REMAP[id] ?: id.takeUnless { it in REMAP.values }

    // A controller with analog triggers has no stick clicks to lose.
    private val REMAP = mapOf(
        JoyconButton.ZL.id to JoyconButton.L.id,
        JoyconButton.ZR.id to JoyconButton.R.id,
        JoyconButton.L.id to JoyconButton.LS.id,
        JoyconButton.R.id to JoyconButton.RS.id,
    )
}
