package com.joegec.joycon2android.update

data class AvailableUpdate(
    val version: AppVersion,
    val highlights: List<String>,
    val downloadUrl: String,
)
