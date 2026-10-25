package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.buttonmapping.DeviceKeysPauseRepository
import com.joegec.joycon2android.buttonmapping.pressesNothing
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceKeysPause : DeviceKeysPauseRepository {
    private val _paused = MutableStateFlow(false)
    val paused: StateFlow<Boolean> = _paused.asStateFlow()
    private var resumeOnRelease = false

    @Synchronized
    override fun setPaused(paused: Boolean) {
        resumeOnRelease = !paused
        if (paused) _paused.value = true
    }

    @Synchronized
    fun onFrame(players: List<PlayerState>) {
        if (resumeOnRelease && players.all { it.pressesNothing() }) {
            resumeOnRelease = false
            _paused.value = false
        }
    }
}
