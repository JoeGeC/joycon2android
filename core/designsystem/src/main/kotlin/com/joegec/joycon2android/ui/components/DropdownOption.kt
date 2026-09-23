package com.joegec.joycon2android.ui.components

/** A disabled option stays visible, dimmed, so its reason can be explained on tap. */
data class DropdownOption(
    val id: String,
    val label: String,
    val subLabel: String? = null,
    val available: Boolean = true,
    val deletable: Boolean = false,
)
