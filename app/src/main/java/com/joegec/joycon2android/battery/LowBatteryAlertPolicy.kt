package com.joegec.joycon2android.battery

import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.settings.LowBatteryAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LowBatteryAlertPolicy(
    private val scope: CoroutineScope,
    private val session: Flow<AppUiState>,
    private val alert: Flow<LowBatteryAlert>,
    private val warn: (ControllerBattery, LowBatteryAlert) -> Unit,
) {
    private val watch = LowBatteryWatch()

    fun start() {
        scope.launch {
            val batteries = session.map { it.controllerBatteries() }.distinctUntilChanged()
            combine(batteries, alert) { batteries, alert -> alert to newlyLow(batteries, alert) }
                .collect { (alert, low) -> low.forEach { warn(it, alert) } }
        }
    }

    private fun newlyLow(batteries: List<ControllerBattery>, alert: LowBatteryAlert): List<ControllerBattery> {
        if (alert.anyWarning) return watch.newlyLow(batteries, alert.warningPercents)
        watch.reset()
        return emptyList()
    }
}
