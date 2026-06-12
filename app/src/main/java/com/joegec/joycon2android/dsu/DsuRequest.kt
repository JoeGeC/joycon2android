package com.joegec.joycon2android.dsu

sealed interface DsuRequest {
    data object Version : DsuRequest
    data class PortInfo(val slots: List<Int>) : DsuRequest

    /** flags: 0 = subscribe to all pads, bit 0 = slot-based, bit 1 = MAC-based. */
    data class PadData(val flags: Int, val slot: Int) : DsuRequest
}
