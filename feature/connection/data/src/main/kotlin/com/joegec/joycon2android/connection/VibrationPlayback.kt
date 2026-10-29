package com.joegec.joycon2android.connection

/** Each effect as timed steps, with the wait after each: docs/protocol.md#vibration */
internal object VibrationPlayback {

    sealed interface Step {
        val gapMs: Long
    }

    class Sample(val command: ByteArray) : Step {
        override val gapMs = 0L
    }

    class Rumble(val amplitude: Int, override val gapMs: Long = 0L) : Step

    private const val CLICK_CLICK: Byte = 0x03

    // A packet holds ~12 ms of rumble, so the next one goes out as it runs out.
    private const val FRAME_MS = 12L
    private const val BUZZ_FRAMES = 3
    private const val FAINT = 0x040

    fun steps(sample: VibrationSample): List<Step> = when (sample) {
        VibrationSample.CLICK -> listOf(Sample(ControllerCommands.vibrationSample(CLICK_CLICK)))
        VibrationSample.BUZZ -> List(BUZZ_FRAMES) { Rumble(FAINT, FRAME_MS) } + Rumble(0)
    }
}
