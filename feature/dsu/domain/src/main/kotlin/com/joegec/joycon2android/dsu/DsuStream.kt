package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerState

/** One DSU slot and the controller state it reports. */
data class DsuStream(val slot: Int, val state: PlayerState)
