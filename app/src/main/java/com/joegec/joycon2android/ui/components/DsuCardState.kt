package com.joegec.joycon2android.ui.components

data class DsuCardState(
    val enabled: Boolean = false,
    val error: String? = null,
    val clientCount: Int = 0,
    val lanEnabled: Boolean = false,
    val showSlotLimitNote: Boolean = false,
)
