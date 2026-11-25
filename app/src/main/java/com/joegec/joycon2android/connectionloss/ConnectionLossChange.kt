package com.joegec.joycon2android.connectionloss

data class ConnectionLossChange(
    val lost: List<LostController>,
    val returned: Set<String>,
)
