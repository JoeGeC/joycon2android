package com.joegec.joycon2android.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joegec.joycon2android.AppContainer
import com.joegec.joycon2android.JoyconApplication
import com.joegec.joycon2android.ble.BlePermissionHandler
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.service.Joycon2Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class Joycon2ViewModel(application: Application) : AndroidViewModel(application) {

    val permissionHandler = BlePermissionHandler(application)

    private var bound = false

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val container: AppContainer
        get() = (getApplication<Application>() as JoyconApplication).container

    private val _permissionDenied = MutableStateFlow(false)
    val permissionDenied: StateFlow<Boolean> = _permissionDenied.asStateFlow()

    // Bound only to keep the service (foreground lifetime) alive; all state is read from
    // the app-scoped container, not the binder.
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }
    }

    init {
        if (permissionHandler.isGranted()) {
            startAndBind()
        }
        // All state comes from the app-scoped container via its use cases, not the binder
        viewModelScope.launch {
            container.observeSession().collect { _uiState.value = it }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (bound) {
            getApplication<Application>().unbindService(connection)
            bound = false
        }
    }

    fun onPermissionsGranted() {
        _permissionDenied.value = false
        if (!bound) startAndBind()
    }

    fun recheckPermissions() {
        if (_permissionDenied.value && permissionHandler.isGranted()) {
            onPermissionsGranted()
        }
    }

    fun startScan() = container.startScan()

    fun stopScan() = container.stopScan()

    fun disconnectAll() = container.disconnectAll()

    fun assignToPlayer(address: String, player: PlayerNumber) = container.assignController(address, player)

    fun unassign(address: String) = container.unassignController(address)

    fun disconnect(address: String) = container.disconnectController(address)

    fun onPermissionsDenied() {
        _permissionDenied.value = true
    }

    /**
     * Stops the service entirely — disconnects all devices and removes the notification.
     * Called when the user explicitly wants to shut everything down.
     */
    fun stopService() {
        container.disconnectAll()
        val app = getApplication<Application>()
        app.stopService(Intent(app, Joycon2Service::class.java))
    }

    private fun startAndBind() {
        val app = getApplication<Application>()
        val intent = Intent(app, Joycon2Service::class.java)
        // Bind only — the service promotes itself to foreground once a Joy-Con connects,
        // so there's no notification while idle
        app.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
}
