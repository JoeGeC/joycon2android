package com.joegec.joycon2android.ui.components

/**
 * One row of an [OptionDropdown]. [subLabel] is the qualifier under the name; an option that
 * cannot be chosen right now stays visible but dimmed, so the reason can be explained on tap.
 */
data class DropdownOption(
    val id: String,
    val label: String,
    val subLabel: String? = null,
    val available: Boolean = true,
    val deletable: Boolean = false,
)
