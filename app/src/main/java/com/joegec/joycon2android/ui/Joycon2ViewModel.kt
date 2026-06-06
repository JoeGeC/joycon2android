package com.joegec.joycon2android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.joegec.joycon2android.ble.Joycon2Manager
import com.joegec.joycon2android.model.Joycon2State
import kotlinx.coroutines.flow.StateFlow

class Joycon2ViewModel(application: Application) : AndroidViewModel(application) {

    private val manager = Joycon2Manager(application)

    val state: StateFlow<Joycon2State> = manager.state

    fun startScan() = manager.startScan()

    fun stop() = manager.stop()

    fun onPermissionsDenied() = manager.emitError("Bluetooth permissions denied")

    override fun onCleared() {
        super.onCleared()
        manager.stop()
    }
}
