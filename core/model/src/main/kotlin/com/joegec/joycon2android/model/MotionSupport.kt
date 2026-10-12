package com.joegec.joycon2android.model

/** What a controller actually measures; console-protocol ones carry no gyroscope. */
enum class MotionSupport {
    Full,
    AccelerometerOnly,
    None,
    ;

    val measuresAcceleration: Boolean
        get() = when (this) {
            Full, AccelerometerOnly -> true
            None -> false
        }

    val measuresRotation: Boolean
        get() = when (this) {
            Full -> true
            AccelerometerOnly, None -> false
        }
}
