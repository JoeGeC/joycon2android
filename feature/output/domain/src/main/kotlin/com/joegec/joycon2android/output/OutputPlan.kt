package com.joegec.joycon2android.output

/** Which devices run: docs/architecture.md#output */
object OutputPlan {

    /**
     * The devices [playing] runs: what the game reads, the pad for getting round the device, and
     * whatever [mapped] keys and clicks ask for. Without Shizuku only DSU can run, so a game that
     * reads buttons there gets them over DSU instead.
     */
    fun devices(playing: Playing, motion: Boolean, shizuku: Boolean, mapped: Set<OutputDevice>): Set<OutputDevice> {
        val game = if (playing.motionOptional && !motion) playing.needs - OutputDevice.MOTION else playing.needs
        val wanted = game + OutputDevice.PAD + mapped
        if (shizuku) return wanted
        return wanted.filterTo(mutableSetOf()) { it !in NEEDS_SHIZUKU } +
            if (playing.buttonsOverDsu) setOf(OutputDevice.MOTION) else emptySet()
    }

    private val NEEDS_SHIZUKU = setOf(OutputDevice.PAD, OutputDevice.KEYBOARD, OutputDevice.MOUSE)
}
