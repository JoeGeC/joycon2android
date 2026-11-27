package com.joegec.joycon2android.widget

import com.joegec.joycon2android.ble.ScanBlocker

enum class HomeWidgetStatus(val isWarning: Boolean = false) {
    NEEDS_PERMISSION(isWarning = true),
    BLUETOOTH_OFF(isWarning = true),
    LOCATION_OFF(isWarning = true),
    NEEDS_SHIZUKU(isWarning = true),
    SCANNING,
    SCANNING_WITH_WAITING,
    WAITING,
    READY,
    ;

    companion object {
        fun of(state: HomeWidgetState): HomeWidgetStatus = when {
            state.blocker == ScanBlocker.PERMISSION -> NEEDS_PERMISSION
            state.blocker == ScanBlocker.BLUETOOTH_OFF -> BLUETOOTH_OFF
            state.blocker == ScanBlocker.LOCATION_OFF -> LOCATION_OFF
            state.needsShizuku -> NEEDS_SHIZUKU
            state.scanning && state.waiting > 0 -> SCANNING_WITH_WAITING
            state.scanning -> SCANNING
            state.waiting > 0 -> WAITING
            else -> READY
        }
    }
}
