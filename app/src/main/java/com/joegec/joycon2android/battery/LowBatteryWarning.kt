package com.joegec.joycon2android.battery

import com.joegec.joycon2android.connection.ControllerRepository
import com.joegec.joycon2android.connection.VibrationSample
import com.joegec.joycon2android.settings.LowBatteryAlert

class LowBatteryWarning(
    private val notification: LowBatteryNotification,
    private val controllers: ControllerRepository,
) {

    fun warn(battery: ControllerBattery, alert: LowBatteryAlert) {
        if (alert.notify) notification.show(battery)
        if (alert.vibrate) controllers.vibrate(battery.address, VibrationSample.LOW_BATTERY)
    }
}
