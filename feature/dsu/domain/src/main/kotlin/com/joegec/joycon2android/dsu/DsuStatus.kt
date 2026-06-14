package com.joegec.joycon2android.dsu

data class DsuStatus(
    val enabled: Boolean = false,
    val error: String? = null,
    val clientCount: Int = 0,
    val address: String? = null,
    val port: Int = DsuConfig.PORT,
)
