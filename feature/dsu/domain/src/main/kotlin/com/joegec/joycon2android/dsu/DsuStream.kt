package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerState

data class DsuStream(
    val slot: Int,
    val state: PlayerState,
    val heldSideways: Boolean = state.isSideways,
)
