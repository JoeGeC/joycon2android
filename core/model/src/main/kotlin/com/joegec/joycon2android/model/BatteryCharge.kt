package com.joegec.joycon2android.model

/** Charge as a percentage: the common report gives volts, the console report a level. */
@JvmInline
value class BatteryCharge(val percent: Int) {

    companion object {
        fun fromVolts(volts: Float): BatteryCharge? =
            if (volts <= 0f) null else BatteryCharge(BatteryGauge.percentFromVolts(volts))

        fun fromLevel(level: Int, maxLevel: Int) = BatteryCharge(level * 100 / maxLevel)
    }
}
