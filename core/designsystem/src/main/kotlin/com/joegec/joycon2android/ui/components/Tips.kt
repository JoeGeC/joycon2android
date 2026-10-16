package com.joegec.joycon2android.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class Tips(val visible: Boolean, val onTap: () -> Unit)

val LocalTips = staticCompositionLocalOf { Tips(visible = true, onTap = {}) }
