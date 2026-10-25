package com.joegec.joycon2android.buttonmapping

/**
 * Paused, a button bound to an Android, keyboard or mouse key reaches the outputs as itself. Unpausing
 * waits for every controller to let go, so the press that ended the pause can't act as it lifts.
 */
interface DeviceKeysPauseRepository {
    fun setPaused(paused: Boolean)
}
