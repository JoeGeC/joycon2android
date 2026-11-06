package com.joegec.joycon2android.output

/** Which devices run: docs/architecture.md#output */
object OutputPlan {

    /**
     * The devices [playing] runs: what the game reads, the pad for getting round the device unless
     * it's optional and [pad] is off, and whatever [mapped] keys and clicks ask for. Without Shizuku only DSU can run, so a game that
     * reads buttons there gets them over DSU instead.
     */
    fun devices(playing: Playing, motion: Boolean, pad: Boolean, shizuku: Boolean, mapped: Set<OutputDevice>): Set<OutputDevice> {
        val game = if (playing.motionOptional && !motion) playing.needs - OutputDevice.MOTION else playing.needs
        val wanted = game + mapped + if (pad || !playing.padOptional) setOf(OutputDevice.PAD) else emptySet()
        if (shizuku) return wanted
        return wanted.filterTo(mutableSetOf()) { it !in NEEDS_SHIZUKU } +
            if (playing.buttonsOverDsu) setOf(OutputDevice.MOTION) else emptySet()
    }

    private val NEEDS_SHIZUKU = setOf(OutputDevice.PAD, OutputDevice.KEYBOARD, OutputDevice.MOUSE)
}
