package com.joegec.joycon2android.dsu

sealed interface DsuRequest {
    data object Version : DsuRequest
    data class PortInfo(val slots: List<Int>) : DsuRequest
    data object PadData : DsuRequest
}
